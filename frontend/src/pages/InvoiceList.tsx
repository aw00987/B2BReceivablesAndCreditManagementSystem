import React, { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Input, InputNumber, DatePicker, message, Select, Descriptions, Spin, Divider } from 'antd';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getInvoices, createInvoice, getInvoiceDetail, invoiceDunning, invoiceLitigation } from '../api/invoiceApi';
import api from '../api/axios';
import useAuthStore from '../store/authStore';

const { Option } = Select;
const { TextArea } = Input;

const InvoiceList: React.FC = () => {
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);
  const [selectedInvoiceNo, setSelectedInvoiceNo] = useState<string | null>(null);
  const [editableNote, setEditableNote] = useState('');

  const { user } = useAuthStore();
  const [form] = Form.useForm();
  const queryClient = useQueryClient();
  const [companyOptions, setCompanyOptions] = useState<{ companyCode: string; companyName: string }[]>([]);

  // 請求書一覧の取得
  const { data, isLoading } = useQuery({
    queryKey: ['invoices'],
    queryFn: () => getInvoices({ page: 0, size: 10 }),
  });

  // 請求書詳細の取得
  const { data: invoice, isLoading: isDetailLoading } = useQuery({
    queryKey: ['invoice', selectedInvoiceNo],
    queryFn: () => getInvoiceDetail(selectedInvoiceNo!),
    enabled: !!selectedInvoiceNo,
  });

  // 同步备注内容到本地状态
  useEffect(() => {
    if (invoice) {
      setEditableNote(invoice.notes || '');
    }
  }, [invoice]);

  // 請求書作成のミューテーション
  const createMutation = useMutation({
    mutationFn: createInvoice,
    onSuccess: () => {
      message.success('請求書が正常に作成されました');
      setIsCreateModalOpen(false);
      form.resetFields();
      queryClient.invalidateQueries({ queryKey: ['invoices'] });
    },
    onError: () => {
      message.error('請求書の作成に失败しました');
    },
  });

  // 督促ミューテーション
  const dunningMutation = useMutation({
    mutationFn: ({ invoiceNo, note }: { invoiceNo: string; note?: string }) => invoiceDunning(invoiceNo, note),
    onSuccess: () => {
      message.success('督促処理が完了しました');
      queryClient.invalidateQueries({ queryKey: ['invoice', selectedInvoiceNo] });
      queryClient.invalidateQueries({ queryKey: ['invoices'] });
    },
    onError: () => message.error('督促処理に失敗しました'),
  });

  // 法的措置ミューテーション
  const litigationMutation = useMutation({
    mutationFn: ({ invoiceNo, note }: { invoiceNo: string; note?: string }) => invoiceLitigation(invoiceNo, note),
    onSuccess: () => {
      message.success('法的措置処理が完了しました');
      queryClient.invalidateQueries({ queryKey: ['invoice', selectedInvoiceNo] });
      queryClient.invalidateQueries({ queryKey: ['invoices'] });
    },
    onError: () => message.error('法的措置処理に失敗しました'),
  });

  const handleCreate = (values: any) => {
    const formattedValues = {
      ...values,
      issueDate: values.issueDate.format('YYYY-MM-DD'),
      dueDate: values.dueDate.format('YYYY-MM-DD'),
      createdBy: user?.username,
    };
    createMutation.mutate(formattedValues);
  };

  const handleSearchCompany = async (value: string) => {
    try {
      const response = await api.get(`/companies/search?userInput=${value}`);
      setCompanyOptions(response.data);
    } catch (error) {
      console.error('Failed to fetch companies', error);
    }
  };

  const columns = [
    { 
      title: 'No.', 
      key: 'index', 
      render: (_: any, __: any, index: number) => index + 1 + ((data?.number || 0) * (data?.size || 10)) 
    },
    { 
      title: '請求书番号', 
      dataIndex: 'invoiceNo', 
      key: 'invoiceNo', 
      render: (text: string) => (
        <Button type="link" onClick={() => {
          setSelectedInvoiceNo(text);
          setIsDetailModalOpen(true);
        }}>
          {text}
        </Button>
      )
    },
    { title: '取引先名', dataIndex: 'companyName', key: 'companyName' },
    { title: '金额', dataIndex: 'invoiceAmount', key: 'invoiceAmount', render: (val: number) => `¥${val.toLocaleString()}` },
    { title: '発行日', dataIndex: 'issueDate', key: 'issueDate' },
    { title: '支払期日', dataIndex: 'dueDate', key: 'dueDate' },
    { 
      title: 'ステータス', 
      dataIndex: 'status', 
      key: 'status', 
    },
  ];

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-2xl font-bold">請求书管理</h1>
        <Button type="primary" onClick={() => setIsCreateModalOpen(true)}>
          請求书作成
        </Button>
      </div>

      <Table
        dataSource={data?.content}
        columns={columns}
        rowKey="invoiceNo"
        loading={isLoading}
        pagination={{
          total: data?.totalElements,
          pageSize: data?.size,
          current: (data?.number || 0) + 1,
        }}
      />

      {/* 請求书作成モーダル */}
      <Modal
        title="請求书作成"
        open={isCreateModalOpen}
        onCancel={() => setIsCreateModalOpen(false)}
        footer={null}
      >
        <Form form={form} layout="vertical" onFinish={handleCreate}>
          <Form.Item name="companyCode" label="取引先名" rules={[{ required: true, message: '取引先名を選択してください' }]}>
            <Select
              showSearch
              placeholder="取引先名を入力して検索"
              defaultActiveFirstOption={false}
              filterOption={false}
              onSearch={handleSearchCompany}
              onFocus={() => handleSearchCompany('')}
              notFoundContent={null}
            >
              {companyOptions.map(d => (
                <Option key={d.companyCode} value={d.companyCode}>{d.companyName}</Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="invoiceAmount" label="金额" rules={[{ required: true, message: '金额を入力してください' }]}>
            <InputNumber style={{ width: '100%' }} formatter={value => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')} parser={value => value!.replace(/\¥\s?|(,*)/g, '')} />
          </Form.Item>
          <Form.Item name="issueDate" label="発行日" rules={[{ required: true, message: '発行日を選択してください' }]}>
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="dueDate" label="支払期日" rules={[{ required: true, message: '支払期日を選択してください' }]}>
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="notes" label="備考">
            <TextArea rows={3} />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={createMutation.isPending} block>
              作成
            </Button>
          </Form.Item>
        </Form>
      </Modal>

      {/* 請求书详细モーダル */}
      <Modal
        title={
          <div className="flex justify-between items-center mr-8">
            <span>請求书详细: {selectedInvoiceNo}</span>
            {invoice && (invoice.status === '延滞' || invoice.status === '督促中') && (
              <Button 
                danger 
                type="primary"
                loading={dunningMutation.isPending || litigationMutation.isPending}
                onClick={() => {
                  if (invoice.status === '延滞') {
                    dunningMutation.mutate({ invoiceNo: invoice.invoiceNo, note: editableNote });
                  } else {
                    litigationMutation.mutate({ invoiceNo: invoice.invoiceNo, note: editableNote });
                  }
                }}
              >
                {invoice.status === '延滞' ? '督促' : '法的措置'}
              </Button>
            )}
          </div>
        }
        open={isDetailModalOpen}
        onCancel={() => {
          setIsDetailModalOpen(false);
          setSelectedInvoiceNo(null);
        }}
        footer={null}
        width={800}
      >
        {isDetailLoading ? (
          <div className="flex justify-center py-10"><Spin size="large" /></div>
        ) : invoice ? (
          <div>
            <Descriptions title="基本情报" bordered column={2}>
              <Descriptions.Item label="請求书番号">{invoice.invoiceNo}</Descriptions.Item>
              <Descriptions.Item label="ステータス">{invoice.status}</Descriptions.Item>
              <Descriptions.Item label="取引先コード">{invoice.companyCode}</Descriptions.Item>
              <Descriptions.Item label="取引先名">{invoice.companyName}</Descriptions.Item>
              <Descriptions.Item label="発行日">{invoice.issueDate}</Descriptions.Item>
              <Descriptions.Item label="支払期日">{invoice.dueDate}</Descriptions.Item>
              <Descriptions.Item label="作成者">{invoice.createdBy}</Descriptions.Item>
            </Descriptions>

            <Divider />

            <Descriptions title="金额详细" bordered column={2}>
              <Descriptions.Item label="請求金额">¥{invoice.invoiceAmount?.toLocaleString()}</Descriptions.Item>
              <Descriptions.Item label="元金">¥{invoice.principalAmount?.toLocaleString()}</Descriptions.Item>
              <Descriptions.Item label="利息起算日">{invoice.interestStartDate || '-'}</Descriptions.Item>
              <Descriptions.Item label="利息金额">
                ¥{invoice.interestAmount ? invoice.interestAmount.toLocaleString() : '0'}
              </Descriptions.Item>
            </Descriptions>

            <Divider />

            <div className="mt-4">
              <div className="font-bold mb-2 text-base">備考</div>
              {(invoice.status === '延滞' || invoice.status === '督促中') ? (
                <TextArea 
                  rows={4} 
                  value={editableNote} 
                  onChange={(e) => setEditableNote(e.target.value)}
                  placeholder="備考を入力してください"
                />
              ) : (
                <div className="p-3 border rounded bg-gray-50 h-32 overflow-y-auto whitespace-pre-wrap text-gray-600">
                  {invoice.notes || '-'}
                </div>
              )}
            </div>
          </div>
        ) : (
          <div>請求书が见つかりません</div>
        )}
      </Modal>
    </div>
  );
};

export default InvoiceList;
