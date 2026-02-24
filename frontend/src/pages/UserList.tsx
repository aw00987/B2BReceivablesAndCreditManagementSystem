import React, { useState } from 'react';
import { Table, Button, Modal, Form, Input, Select, message, Popconfirm } from 'antd';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getUsers, createUser, disableUser } from '../api/userApi';

const { Option } = Select;

const UserList: React.FC = () => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [form] = Form.useForm();
  const queryClient = useQueryClient();
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10 });

  const { data, isLoading } = useQuery({
    queryKey: ['users', pagination.current, pagination.pageSize],
    queryFn: () => getUsers({ page: pagination.current - 1, size: pagination.pageSize }),
  });

  const createMutation = useMutation({
    mutationFn: createUser,
    onSuccess: () => {
      message.success('ユーザーが正常に作成されました');
      setIsModalOpen(false);
      form.resetFields();
      queryClient.invalidateQueries({ queryKey: ['users'] });
    },
    onError: () => {
      message.error('ユーザーの作成に失敗しました');
    },
  });

  const disableMutation = useMutation({
    mutationFn: disableUser,
    onSuccess: () => {
      message.success('ユーザーが無効化されました');
      queryClient.invalidateQueries({ queryKey: ['users'] });
    },
    onError: () => {
      message.error('ユーザーの無効化に失敗しました');
    },
  });

  const handleCreate = (values: any) => {
    createMutation.mutate(values);
  };

  const handleDisable = (username: string) => {
    disableMutation.mutate(username);
  };

  const handleTableChange = (newPagination: any) => {
    setPagination({
      current: newPagination.current,
      pageSize: newPagination.pageSize,
    });
  };

  const columns = [
    {
      title: '#',
      key: 'index',
      render: (_: any, __: any, index: number) => (pagination.current - 1) * pagination.pageSize + index + 1,
    },
    { title: 'ユーザー名', dataIndex: 'username', key: 'username' },
    { title: '氏名', dataIndex: 'realName', key: 'realName' },
    { title: '役割', dataIndex: 'role', key: 'role' },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: any) => (
        <Popconfirm
          title="このユーザーを無効にしますか？"
          onConfirm={() => handleDisable(record.username)}
          okText="はい"
          cancelText="いいえ"
        >
          <Button type="link" danger>
            無効にする
          </Button>
        </Popconfirm>
      ),
    },
  ];

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-2xl font-bold">ユーザー管理</h1>
        <Button type="primary" onClick={() => setIsModalOpen(true)}>
          ユーザー作成
        </Button>
      </div>

      <Table
        dataSource={data?.content}
        columns={columns}
        rowKey="username"
        loading={isLoading}
        pagination={{
          total: data?.totalElements,
          pageSize: pagination.pageSize,
          current: pagination.current,
          showSizeChanger: true,
        }}
        onChange={handleTableChange}
      />

      <Modal
        title="ユーザー作成"
        open={isModalOpen}
        onCancel={() => setIsModalOpen(false)}
        footer={null}
      >
        <Form form={form} layout="vertical" onFinish={handleCreate}>
          <Form.Item name="username" label="ユーザー名" rules={[{ required: true, message: 'ユーザー名を入力してください' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="password" label="パスワード" rules={[{ required: true, message: 'パスワードを入力してください' }]}>
            <Input.Password />
          </Form.Item>
          <Form.Item name="realName" label="氏名" rules={[{ required: true, message: '氏名を入力してください' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="role" label="役割" rules={[{ required: true, message: '役割を選択してください' }]}>
            <Select>
              <Option value="ADMIN">管理者</Option>
              <Option value="SALES">営業マン</Option>
              <Option value="LEGAL">法務担当</Option>
              <Option value="FINANCE">財務担当</Option>
            </Select>
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={createMutation.isPending} block>
              作成
            </Button>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default UserList;