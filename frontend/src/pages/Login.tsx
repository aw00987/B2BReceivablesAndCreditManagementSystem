import React from 'react';
import { Form, Input, Button, Card, message } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { login } from '../api/authApi';
import useAuthStore from '../store/authStore';
//todo: import CryptoJS from 'crypto-js';

const Login: React.FC = () => {
  const navigate = useNavigate();
  const setAuth = useAuthStore((state) => state.setAuth);

  const loginMutation = useMutation({
    mutationFn: login,
    onSuccess: (data) => {
      setAuth(data.token, { username: data.username, role: data.role });
      message.success('ログインに成功しました');
      navigate('/dashboard');
    },
    onError: () => {
      message.error('ログインに失敗しました。認証情報を確認してください。');
    },
  });

  const onFinish = (values: any) => {
    //todo: const hashedPassword = CryptoJS.SHA256(values.password).toString();
    loginMutation.mutate({ ...values, password: values.password });
  };

  return (
    <div className="flex justify-center items-center h-screen bg-gray-100">
      <Card title="売掛金管理システム" style={{ width: 400 }}>
        <Form
          name="login"
          initialValues={{ remember: true }}
          onFinish={onFinish}
          layout="vertical"
        >
          <Form.Item
            name="username"
            rules={[{ required: true, message: 'ユーザー名を入力してください！' }]}
          >
            <Input prefix={<UserOutlined />} placeholder="ユーザー名" />
          </Form.Item>
          <Form.Item
            name="password"
            rules={[{ required: true, message: 'パスワードを入力してください！' }]}
          >
            <Input.Password prefix={<LockOutlined />} placeholder="パスワード" />
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" loading={loginMutation.isPending} block>
              ログイン
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default Login;