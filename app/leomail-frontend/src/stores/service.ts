import axios from "axios";

export class Service{
    private static instance: Service;

    public static getInstance(): Service {
        if (!Service.instance) {
            Service.instance = new Service();
        }
        return Service.instance;
    }

    public addTemplate(formData: any){
        console.log("du schickst gleich die daten");
        return axios.post(`/api/template/add`, formData);
    }

    public getVorlagen(){
        return axios.get(`/api/template/all`);
    }

    public getGreetings(){
        return axios.get(`/api/template/greetings`);
    }

    public getGreetingById(id: number) {
        return axios.get(`/api/greeting?gid=${id}`)
    }

    public removeTemplate(id: any){
        return axios.delete(`/api/template/delete?tid=${id}`);
    }

    public updateTemplate(formData: any){
        console.log(formData)
        return axios.post(`/api/template/update`, formData);
    }

    public sendEmails(formData: any){
        console.log("du schickst gleich die daten");
        return axios.post(`/api/mail/sendByTemplate`, formData);
    }

    public getSendEmails(){
        return axios.get(`/api/template/getUsedTemplates?scheduled=false`);
    }

    public addContact(formData: any) {
        return axios.post(`/api/users/add`, formData)
    }

    public async searchContacts(query: string) {
        console.log('query: ' + query)
        return axios.get(`/api/users/search`, {
            params: {
                "query": query
            }})};

    public getContacts(){
        return axios.get(`/api/users/get`);
    }

    public updateContact(id: any, formData: any){
        return axios.post(`/api/users/update?id=${formData}`, formData);
    }

    public deleteContact(id: any){
        return axios.post(`/api/users/delete`, {
            params: {
                "id": id
            }
        });
    }


}