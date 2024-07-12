import axios from "axios";

export class Service{
    private static instance: Service;
    private baseUrl: string;

    private constructor() {
        this.baseUrl = 'http://localhost:8080/';
    }

    public static getInstance(): Service {
        if (!Service.instance) {
            Service.instance = new Service();
        }
        return Service.instance;
    }

    public addTemplate(formData: any){
        console.log("du schickst gleich die daten");
        return axios.post(`${this.baseUrl}/template/add`, formData);
    }

    public getVorlagen(){
        return axios.get(`${this.baseUrl}/template/all`);
    }

    public getGreetings(){
        return axios.get(`${this.baseUrl}/template/greetings`);
    }
}