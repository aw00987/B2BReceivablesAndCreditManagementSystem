import api from './axios';

export interface Invoice {
  id: number;
  invoiceNo: string;
  companyCode: string;
  companyName: string;
  invoiceAmount: number;
  issueDate: string;
  dueDate: string;
  createdBy: string;
  notes?: string;
  status: string;
  principalAmount: number;
  interestStartDate?: string;
  interestAmount?: number;
}

export const getInvoices = async (params: { status?: string; companyCode?: string; page?: number; size?: number }) => {
  const response = await api.get('/invoices', { params });
  return response.data;
};

export const getInvoiceDetail = async (invoiceNo: string) => {
  const response = await api.get(`/invoices/${invoiceNo}`);
  return response.data;
};

export const createInvoice = async (invoice: Partial<Invoice>) => {
  const response = await api.post('/invoices', invoice);
  return response.data;
};
export const invoiceDunning = async (invoiceNo: string, note?: string) => {
  const response = await api.put(`/invoices/${invoiceNo}/dunning`, null, { params: { note } });
  return response.data;
};

export const invoiceLitigation = async (invoiceNo: string, note?: string) => {
  const response = await api.put(`/invoices/${invoiceNo}/litigation`, null, { params: { note } });
  return response.data;
};
