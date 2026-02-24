import axios from 'axios';

const api = axios.create({
    baseURL: 'http://localhost:8080/api',
    withCredentials: true, // Send cookies for sessions
    headers: {
        'Content-Type': 'application/json',
    },
});

export default api;
