import axios from 'axios';

const API_URL = 'http://localhost:8080/api/analysis'; // Update if deployed

const getAnalysis = async (symbol: string) => {
    const response = await axios.get(`${API_URL}/${symbol}`);
    return response.data;
};

export default getAnalysis;
