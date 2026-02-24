import React, { useState } from 'react';
import { Upload, Button, message, Card, Row, Col, Statistic, Space, Typography } from 'antd';
import { UploadOutlined, SyncOutlined, CheckCircleOutlined } from '@ant-design/icons';
import { useMutation } from '@tanstack/react-query';
import { autoReconcile } from '../api/reconciliationApi';

const { Paragraph, Text } = Typography;

const Reconciliation: React.FC = () => {
  const [autoResult, setAutoResult] = useState<{ matchedCount: number; totalAmount: number } | null>(null);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);

  const autoReconcileMutation = useMutation({
    mutationFn: autoReconcile,
    onSuccess: (data) => {
      setAutoResult(data);
      message.success(`消込が完了しました。一致件数: ${data.matchedCount}`);
    },
    onError: (error: any) => {
      message.error(`消込処理に失敗しました: ${error.message || '不明なエラー'}`);
    },
  });

  const handleReconcile = () => {
    if (selectedFile) {
      autoReconcileMutation.mutate(selectedFile);
    } else {
      message.warning('まずCSVファイルを選択してください。');
    }
  };

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold mb-6">入金消込</h1>
      
      <Card title="自動消込（CSVアップロード）" bordered={false} className="shadow-sm max-w-2xl">
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          <div>
            <Paragraph>
              <Text strong>ステップ 1:</Text> 銀行取引データを含むCSVファイルを選択します。
            </Paragraph>
            <Upload 
              beforeUpload={(file) => {
                setSelectedFile(file);
                return false; // Prevent default upload behavior
              }}
              onRemove={() => setSelectedFile(null)}
              fileList={selectedFile ? [selectedFile as any] : []}
              maxCount={1}
              accept=".csv"
            >
              <Button icon={<UploadOutlined />} size="large">
                {selectedFile ? 'ファイルを変更' : 'CSVファイルを選択'}
              </Button>
            </Upload>
            <Paragraph type="secondary" className="mt-2">
              <Text strong>ヒント:</Text> 全銀フォーマットのCSVファイルをアップロードしてください。
            </Paragraph>
          </div>

          <div className="pt-4 border-t border-gray-50">
            <Paragraph>
              <Text strong>ステップ 2:</Text> ファイルを選択したら、下のボタンをクリックして消込を開始します。
            </Paragraph>
            <Button 
              type="primary" 
              size="large"
              icon={<SyncOutlined spin={autoReconcileMutation.isPending} />} 
              onClick={handleReconcile}
              disabled={!selectedFile || autoReconcileMutation.isPending}
              loading={autoReconcileMutation.isPending}
              style={{ width: '200px' }}
            >
              消込を実行
            </Button>
          </div>
        </Space>

        {autoResult && (
          <div className="mt-8 pt-8 border-t border-gray-100">
            <h3 className="text-lg font-medium mb-4">消込結果</h3>
            <Row gutter={16}>
              <Col span={12}>
                <Statistic 
                  title="一致件数" 
                  value={autoResult.matchedCount} 
                  prefix={<CheckCircleOutlined className="text-green-500" />}
                />
              </Col>
              <Col span={12}>
                <Statistic 
                  title="合計金額" 
                  value={autoResult.totalAmount} 
                  prefix="¥" 
                  precision={0} 
                />
              </Col>
            </Row>
          </div>
        )}
      </Card>
    </div>
  );
};

export default Reconciliation;