import React, { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Input, InputNumber, message, Descriptions } from 'antd';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getCompanies, createCompany, Company } from '../api/companyApi';
import useAuthStore from '../store/authStore';

const CompanyList: React.FC = () => {
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);
  const [selectedCompany, setSelectedCompany] = useState<Company | null>(null);
  const [form] = Form.useForm();
  const queryClient = useQueryClient();
  const { user } = useAuthStore();

  const { data, isLoading } = useQuery({
    queryKey: ['companies'],
    queryFn: () => getCompanies({ page: 0, size: 10 }),
  });

  useEffect(() => {
    if (isCreateModalOpen && user) {
      form.setFieldsValue({ picUsername: user.username });
    }
  }, [isCreateModalOpen, user, form]);

  const createMutation = useMutation({
    mutationFn: createCompany,
    onSuccess: () => {
      message.success('取引先が正常に登録されました');
      setIsCreateModalOpen(false);
      form.resetFields();
      queryClient.invalidateQueries({ queryKey: ['companies'] });
    },
    onError: () => {
      message.error('取引先の登録に失敗しました');
    },
  });

  const handleCreate = (values: any) => {
    createMutation.mutate(values);
  };

  const showDetails = (record: Company) => {
    setSelectedCompany(record);
    setIsDetailModalOpen(true);
  };

  const columns = [
    {
      title: 'ID',
      key: 'index',
      render: (_: any, __: any, index: number) => {
        const page = data?.number || 0;
        const size = data?.size || 10;
        return page * size + index + 1;
      },
    },
    {
      title: '取引先コード',
      dataIndex: 'companyCode',
      key: 'companyCode',
      render: (text: string, record: Company) => (
          <Button type="link" onClick={() => showDetails(record)} style={{ padding: 0 }}>
            {text}
          </Button>
      ),
    },
    { title: '取引先名', dataIndex: 'companyName', key: 'companyName' },
    { title: '与信ランク', dataIndex: 'creditRating', key: 'creditRating' },
    { title: '与信限度額（¥）', dataIndex: 'creditLimit', key: 'creditLimit', render: (val: number) => `${val.toLocaleString()}` },
    { title: '担当者', dataIndex: 'picRealName', key: 'picRealName' },
  ];

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-2xl font-bold">取引先管理</h1>
        <Button type="primary" onClick={() => setIsCreateModalOpen(true)}>
          取引先登録
        </Button>
      </div>

      <Table
        dataSource={data?.content}
        columns={columns}
        rowKey="companyCode"
        loading={isLoading}
        pagination={{
          total: data?.totalElements,
          pageSize: data?.size,
          current: (data?.number || 0) + 1,
        }}
      />

      {/* Create Modal */}
      <Modal
        title="取引先登録"
        open={isCreateModalOpen}
        onCancel={() => setIsCreateModalOpen(false)}
        footer={null}
        width={600}
      >
        <Form form={form} layout="vertical" onFinish={handleCreate}>
          <div className="grid grid-cols-2 gap-4">
            <Form.Item name="companyCode" label="取引先コード" rules={[{ required: true, message: '取引先コードを入力してください' }]}>
              <Input />
            </Form.Item>
            <Form.Item name="companyName" label="取引先名" rules={[{ required: true, message: '取引先名を入力してください' }]}>
              <Input />
            </Form.Item>
            <Form.Item name="creditRating" label="与信ランク" rules={[{ required: true, message: '与信ランクを入力してください' }]}>
              <Input />
            </Form.Item>
            <Form.Item name="creditLimit" label="与信限度額（¥）" rules={[{ required: true, message: '与信限度額を入力してください' }]}>
              <InputNumber style={{ width: '100%' }} formatter={value => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')} parser={value => value!.replace(/\¥\s?|(,*)/g, '')} />
            </Form.Item>
            <Form.Item name="picUsername" label="担当者ユーザー名" rules={[{ required: true, message: '担当者ユーザー名を入力してください' }]}>
              <Input />
            </Form.Item>
            <Form.Item name="email" label="メールアドレス" rules={[{ type: 'email', message: '有効なメールアドレスを入力してください' }]}>
              <Input />
            </Form.Item>
            <Form.Item name="phoneNum" label="電話番号">
              <Input />
            </Form.Item>
            <Form.Item name="faxNum" label="FAX番号">
              <Input />
            </Form.Item>
          </div>
          <Form.Item name="address" label="住所" rules={[{ required: true, message: '住所を入力してください' }]}>
            <Input.TextArea rows={2} />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={createMutation.isPending} block>
              登録
            </Button>
          </Form.Item>
        </Form>
      </Modal>

      {/* Detail Modal */}
      <Modal
        title="取引先詳細"
        open={isDetailModalOpen}
        onCancel={() => setIsDetailModalOpen(false)}
        footer={[
          <Button key="close" onClick={() => setIsDetailModalOpen(false)}>
            閉じる
          </Button>
        ]}
        width={700}
      >
        {selectedCompany && (
          <Descriptions bordered column={1}>
            <Descriptions.Item label="取引先コード">{selectedCompany.companyCode}</Descriptions.Item>
            <Descriptions.Item label="取引先名">{selectedCompany.companyName}</Descriptions.Item>
            <Descriptions.Item label="与信ランク">{selectedCompany.creditRating}</Descriptions.Item>
            <Descriptions.Item label="与信限度額（¥）">{selectedCompany.creditLimit.toLocaleString()}</Descriptions.Item>
            <Descriptions.Item label="担当者ユーザー名">{selectedCompany.picUsername}</Descriptions.Item>
            <Descriptions.Item label="担当者氏名">{selectedCompany.picRealName}</Descriptions.Item>
            <Descriptions.Item label="メールアドレス">{selectedCompany.email}</Descriptions.Item>
            <Descriptions.Item label="電話番号">{selectedCompany.phoneNum}</Descriptions.Item>
            <Descriptions.Item label="FAX番号">{selectedCompany.faxNum}</Descriptions.Item>
            <Descriptions.Item label="住所">{selectedCompany.address}</Descriptions.Item>
          </Descriptions>
        )}
      </Modal>
    </div>
  );
};

export default CompanyList;
