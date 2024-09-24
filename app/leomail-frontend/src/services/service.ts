import axios from "axios";

export class Service {
    private static instance: Service;

    public static getInstance(): Service {
        if (!Service.instance) {
            Service.instance = new Service();
        }
        return Service.instance;
    }

    public addTemplate(formData: any) {
        console.log("du schickst gleich die daten");
        return axios.post(`/api/template/add`, formData);
    }

    public getTemplates(projectId: string) {
        return axios.get(`/api/template/get`, {
            params: {
                "pid": projectId
            }
        });
    }

    public getGreetings() {
        return axios.get(`/api/template/greetings`);
    }

    public removeTemplate(templateId: any, projectId: string) {
        return axios.delete(`/api/template/delete`, {
            params: {
                "tid": templateId,
                "pid": projectId
            }
        });
    }

    public updateTemplate(formData: any) {
        console.log(formData)
        return axios.post(`/api/template/update`, formData);
    }

    public sendEmails(formData: any, projectId: string) {
        console.log("du schickst gleich die daten");
        return axios.post(`/api/mail/sendByTemplate`, formData, {
            params: {
                "pid": projectId
            }
        });
    }

    public getUsedTemplates(scheduled: boolean, projectId: string) {
        return axios.get(`/api/template/getUsedTemplates`, {
            params: {
                "scheduled": scheduled,
                "pid": projectId
            }
        });
    }

    public addContact(formData: any) {
        return axios.post(`/api/users/add`, formData)
    }

    public async searchContacts(query: string) {
        console.log('query: ' + query)
        return axios.get(`/api/users/search`, {
            params: {
                "query": query
            }
        })
    };

    public getContacts() {
        return axios.get(`/api/users/get`);
    }

    public getContact(id: string) {
        return axios.get(`/api/users/single`, {
            params: {
                "id": id
            }
        })
    };

    public updateContact(id: any, formData: any) {
        return axios.post(`/api/users/update?id=${formData}`, formData);
    }

    public deleteContact(id: any){
        return axios.post(`/api/users/delete`, {}, {
            params: {
                "id": id
            }
        });
    }
    public getPersonalProjects(){
        return axios.get(`/api/project/get/personal`);
    }

    public checkPermission(projectId: string) {
        return axios.get(`/api/permission/check`, {
            params: {
                "pid": projectId
            }
        });
    }

    async getPersonalGroups(projectId: string) {
        return axios.get<Group>(`/api/groups/get/personal`, {
            params: {
                "pid": projectId
            }
        });
    }

    async addGroup(projectId: string, groupData: Group) {
        return axios.post(`/api/groups/add`, groupData, {
            params: {
                "pid": projectId
            }
        });
    }

    async updateGroup(projectId: string, groupData: Group) {
        return axios.post(`/api/groups/update`, groupData, {
            params: {
                "pid": projectId
            }

        });
    }

    async deleteGroup(projectId: string, groupId: string) {
        return axios.delete(`/api/groups/delete`, {
            params: {
                "pid": projectId,
                "gid": groupId
            }
        });
    }

    async getGroupDetails(groupId: string, projectId: string) {
        return axios.get<Group>(`/api/groups/get/details`, {
            params: {
                "pid": projectId,
                "gid": groupId
            }
        });
    }

    async getUsersInGroup(groupId: string, projectId: string) {
        return axios.get(`/api/groups/getUsers`, {
            params: {
                "pid": projectId,
                "gid": groupId
            }
        })
    }
}

interface Group {
    id: undefined | string;
    name: string;
    description: string;
    members: string[];
}