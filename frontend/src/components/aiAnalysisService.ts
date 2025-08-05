import axios from 'axios';
import { API_ENDPOINTS } from '../config/api';

const getAnalysis = async (symbol: string) => {
    const response = await axios.get(`${API_ENDPOINTS.ANALYSIS}/${symbol}`);
    return response.data;
};

export default getAnalysis;
