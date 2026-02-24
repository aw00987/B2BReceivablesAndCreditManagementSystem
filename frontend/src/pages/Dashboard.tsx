import React, { useEffect, useState } from 'react';
import { Card, Col, Row, Statistic, Spin, message } from 'antd';
import axios from 'axios';

interface DashboardStatistics {
  totalReceivableAmount: number;
  overdueAmount: number;
  collectionRate: number;
}

const Dashboard: React.FC = () => {
  const [loading, setLoading] = useState<boolean>(true);
  const [stats, setStats] = useState<DashboardStatistics | null>(null);

  useEffect(() => {
    const fetchStatistics = async () => {
      try {
        const response = await axios.get('/api/v1/invoices/statistics');
        setStats(response.data);
      } catch (error) {
        console.error('Failed to fetch statistics:', error);
        message.error('統計データの取得に失敗しました。');
      } finally {
        setLoading(false);
      }
    };

    fetchStatistics();
  }, []);

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <Spin size="large" />
      </div>
    );
  }

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">ダッシュボード</h1>
      <Row gutter={16}>
        <Col span={8}>
          <Card bordered={false}>
            <Statistic
              title="売掛金総額"
              value={stats?.totalReceivableAmount || 0}
              precision={0}
              prefix="¥"
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card bordered={false}>
            <Statistic
              title="延滞金額"
              value={stats?.overdueAmount || 0}
              precision={0}
              prefix="¥"
              valueStyle={{ color: (stats?.overdueAmount || 0) > 0 ? '#cf1322' : undefined }}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card bordered={false}>
            <Statistic
              title="回収率"
              value={stats?.collectionRate || 0}
              precision={2}
              suffix="%"
            />
          </Card>
        </Col>
      </Row>
      
      <div className="mt-8">
        <Card title="最近のアクティビティ">
          <p>システムが初期化されました。売掛金管理システム へようこそ。</p>
        </Card>
      </div>
    </div>
  );
};

export default Dashboard;
