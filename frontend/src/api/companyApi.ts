import api from './axios';

export interface Company {
    companyCode: string;
    companyName: string;
    creditRating: string;
    creditLimit: number;
    picUsername: string;
    picRealName?: string;
    email: string;
    phoneNum: string;
    faxNum: string;
    address: string;
}

export const getCompanies = async (params: { page?: number; size?: number }) => {
    const response = await api.get('/companies', {params});
    return response.data;
};

export const createCompany = async (company: Omit<Company, 'picRealName'>) => {
    const response = await api.post('/companies', company);
    return response.data;
};
