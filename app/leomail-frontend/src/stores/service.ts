import axios from "axios";

export class Service{
    private static instance: Service;
    private baseUrl: string;

    private constructor() {
        this.baseUrl = 'http://localhost:8080/test';
    }

    public static getInstance(): Service {
        if (!Service.instance) {
            Service.instance = new Service();
        }
        return Service.instance;
    }

    public postVorlage(formData: any){
        return axios.post(`${this.baseUrl}/post`, formData);
    }

    public getVorlagen(){
        return axios.get(`${this.baseUrl}/get`);
    }
}