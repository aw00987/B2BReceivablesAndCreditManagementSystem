import React, {useState} from 'react';
import {Layout, Menu, Button, theme} from 'antd';
import {
    MenuFoldOutlined,
    MenuUnfoldOutlined,
    DashboardOutlined,
    UserOutlined,
    TeamOutlined,
    FileTextOutlined,
    BankOutlined,
    LogoutOutlined,
} from '@ant-design/icons';
import {Outlet, useNavigate, useLocation} from 'react-router-dom';
import useAuthStore from '../store/authStore';

const {Header, Sider, Content} = Layout;

const MainLayout: React.FC = () => {
    const [collapsed, setCollapsed] = useState(false);
    const {
        token: {colorBgContainer, borderRadiusLG},
    } = theme.useToken();
    const navigate = useNavigate();
    const location = useLocation();
    const logout = useAuthStore((state) => state.logout);
    const user = useAuthStore((state) => state.user);

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const menuItems = [
        {
            key: '/dashboard',
            icon: <DashboardOutlined/>,
            label: 'ダッシュボード',
            onClick: () => navigate('/dashboard'),
        },
        {
            key: '/users',
            icon: <UserOutlined/>,
            label: 'ユーザー管理',
            onClick: () => navigate('/users'),
            // Only admin can see user management
            hidden: user?.role !== 'ADMIN'
        },
        {
            key: '/companies',
            icon: <TeamOutlined/>,
            label: '取引先管理',
            onClick: () => navigate('/companies'),
        },
        {
            key: '/invoices',
            icon: <FileTextOutlined/>,
            label: '請求書管理',
            onClick: () => navigate('/invoices'),
        },
        {
            key: '/reconciliation',
            icon: <BankOutlined/>,
            label: '入金消込',
            onClick: () => navigate('/reconciliation'),
        },
    ].filter(item => !item.hidden);

    return (
        <Layout style={{minHeight: '100vh'}}>
            <Sider trigger={null} collapsible collapsed={collapsed}>
                <div className="demo-logo-vertical" style={{
                    height: 32,
                    margin: 16,
                    background: 'rgba(255, 255, 255, 0.2)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    color: 'white',
                    fontWeight: 'bold',
                    whiteSpace: 'nowrap',
                    overflow: 'hidden'
                }}>
                    {collapsed ? '売掛金' : '売掛金管理システム'}
                </div>
                <Menu
                    theme="dark"
                    mode="inline"
                    selectedKeys={[location.pathname]}
                    items={menuItems}
                />
            </Sider>
            <Layout>
                <Header style={{
                    padding: 0,
                    background: colorBgContainer,
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center'
                }}>
                    <Button
                        type="text"
                        icon={collapsed ? <MenuUnfoldOutlined/> : <MenuFoldOutlined/>}
                        onClick={() => setCollapsed(!collapsed)}
                        style={{
                            fontSize: '16px',
                            width: 64,
                            height: 64,
                        }}
                    />
                    <div style={{marginRight: 24, display: 'flex', alignItems: 'center', gap: 16}}>
                        <span>ようこそ、{user?.username}</span>
                        <Button icon={<LogoutOutlined/>} onClick={handleLogout}>ログアウト</Button>
                    </div>
                </Header>
                <Content
                    style={{
                        margin: '24px 16px',
                        padding: 24,
                        minHeight: 280,
                        background: colorBgContainer,
                        borderRadius: borderRadiusLG,
                        overflow: 'auto'
                    }}
                >
                    <Outlet/>
                </Content>
            </Layout>
        </Layout>
    );
};

export default MainLayout;