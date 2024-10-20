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

    /**
     * Adds a new natural contact.
     */
    public addNaturalContact(contactData: any) {
        return axios.post(`/api/contacts/add/natural`, contactData);
    }

    public getUsedTemplate(tid: number, pid: string) {
        return axios.get(`/api/template/getUsedTemplate`, {
            params: {
                "tid": tid,
                "pid": pid
            }
        });
    }

    /**
     * Adds a new company contact.
     */
    public addCompanyContact(contactData: any) {
        return axios.post(`/api/contacts/add/company`, contactData);
    }

    /**
     * Updates an existing natural contact.
     */
    public updateNaturalContact(contactData: any) {
        return axios.post(`/api/contacts/update/natural`, contactData);
    }

    /**
     * Updates an existing company contact.
     */
    public updateCompanyContact(contactData: any) {
        return axios.post(`/api/contacts/update/company`, contactData);
    }

    /**
     * Deletes a contact by ID.
     */
    public deleteContact(id: string) {
        return axios.delete(`/api/contacts/delete`, {
            params: {
                id: id,
            },
        });
    }

    /**
     * Retrieves a single contact by ID.
     */
    public getContact(id: string) {
        return axios.get(`/api/contacts/single`, {
            params: {
                id: id,
            },
        });
    }

    /**
     * Searches for natural contacts.
     */
    public searchNaturalContacts(query: string) {
        return axios.get(`/api/contacts/search/natural`, {
            params: {
                query: query,
            },
        });
    }

    /**
     * Searches for company contacts.
     */
    public searchCompanyContacts(query: string) {
        return axios.get(`/api/contacts/search/company`, {
            params: {
                query: query,
            },
        });
    }

    /**
     * Searches for all contacts.
     */
    public searchAllContacts(query: string) {
        return axios.get(`/api/contacts/search/all`, {
            params: {
                query: query,
            },
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

    public async getPersonalGroups(projectId: string) {
        return axios.get(`/api/groups/get/personal?pid=${projectId}`);
    }

    public async getGroupDetails(projectId: string, groupId: string) {
        return axios.get('/api/groups/get/details', {
            params: {
                pid: projectId,
                gid: groupId
            }
        })
    }

    public async addGroup(projectId: string, groupData: any) {
        const groupType = groupData.groupType || "NATURAL"; // Adjust as needed
        return axios.post(`/api/groups/add?pid=${projectId}&groupType=${groupType}`, groupData);
    }

    public async updateGroup(projectId: string, groupData: any) {
        const groupType = groupData.groupType || "NATURAL"; // Adjust as needed
        return axios.post(`/api/groups/update?pid=${projectId}&groupType=${groupType}`, groupData);
    }

    public async deleteGroup(projectId: string, groupId: string) {
        return axios.delete(`/api/groups/delete?pid=${projectId}&gid=${groupId}`);
    }

    public async searchGroups(projectId: string, query: string) {
        return axios.get(`/api/groups/search?pid=${projectId}&query=${query}`);
    }

    async getUsersInGroup(groupId: string, projectId: string) {
        return axios.get(`/api/groups/getUsers`, {
            params: {
                "pid": projectId,
                "gid": groupId
            }
        })
    }

    async getProfile() {
        return axios.get(`/api/auth/profile`);
    }

    async getProjectName(projectId: string) {
        return axios.get(`/api/project/get/name`, {
            params: {
                "pid": projectId
            }
        })
    }

    /**
     * Search for contacts (both Natural and Company).
     * @param query The search term.
     * @param projectId The current project ID.
     * @returns A promise resolving to the search results.
     */
    public async searchContacts(query: string, projectId: string) {
        return axios.get(`/api/contacts/search/all`, {
            params: {
                query: query,
                projectId: projectId
            }
        });
    }

    async saveOutlookPassword(formData: { email: string; password: string }) {
        return axios.post(`/api/auth/save-outlook-password`, formData);
    }

    async checkOutlookAuthorization(): Promise<boolean> {
        try {
            const response = await axios.get('/api/auth/check-outlook-authorization');
            return response.data; // Expected response: { isAuthorized: true/false }
        } catch (error) {
            console.error('Outlook-Autorisierungsprüfung fehlgeschlagen:', error);
            throw error;
        }
    }
}