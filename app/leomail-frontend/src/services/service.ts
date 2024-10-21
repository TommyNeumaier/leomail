import axios from "axios";
import axiosInstance from "@/axiosInstance";

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
        return axiosInstance.post(`/template/add`, formData);
    }

    public getTemplates(projectId: string) {
        return axiosInstance.get(`/template/get`, {
            params: {
                "pid": projectId
            }
        });
    }

    public getGreetings() {
        return axiosInstance.get(`/template/greetings`);
    }

    public removeTemplate(templateId: any, projectId: string) {
        return axiosInstance.delete(`/template/delete`, {
            params: {
                "tid": templateId,
                "pid": projectId
            }
        });
    }

    public updateTemplate(formData: any) {
        console.log(formData)
        return axiosInstance.post(`/template/update`, formData);
    }

    public sendEmails(formData: any, projectId: string) {
        console.log("du schickst gleich die daten");
        return axiosInstance.post(`/mail/sendByTemplate`, formData, {
            params: {
                "pid": projectId
            }
        });
    }

    public getUsedTemplates(scheduled: boolean, projectId: string) {
        return axiosInstance.get(`/template/getUsedTemplates`, {
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
        return axiosInstance.post(`/contacts/add/natural`, contactData);
    }

    public getUsedTemplate(tid: number, pid: string) {
        return axiosInstance.get(`/template/getUsedTemplate`, {
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
        return axiosInstance.post(`/contacts/add/company`, contactData);
    }

    /**
     * Updates an existing natural contact.
     */
    public updateNaturalContact(contactData: any) {
        return axiosInstance.post(`/contacts/update/natural`, contactData);
    }

    /**
     * Updates an existing company contact.
     */
    public updateCompanyContact(contactData: any) {
        return axiosInstance.post(`/contacts/update/company`, contactData);
    }

    /**
     * Deletes a contact by ID.
     */
    public deleteContact(id: string) {
        return axiosInstance.delete(`/contacts/delete`, {
            params: {
                id: id,
            },
        });
    }

    /**
     * Retrieves a single contact by ID.
     */
    public getContact(id: string) {
        return axiosInstance.get(`/contacts/single`, {
            params: {
                id: id,
            },
        });
    }

    /**
     * Searches for natural contacts.
     */
    public searchNaturalContacts(query: string) {
        return axiosInstance.get(`/contacts/search/natural`, {
            params: {
                query: query,
            },
        });
    }

    /**
     * Searches for company contacts.
     */
    public searchCompanyContacts(query: string) {
        return axiosInstance.get(`/contacts/search/company`, {
            params: {
                query: query,
            },
        });
    }

    /**
     * Searches for all contacts.
     */
    public searchAllContacts(query: string) {
        return axiosInstance.get(`/contacts/search/all`, {
            params: {
                query: query,
            },
        });
    }


    public getPersonalProjects(){
        return axiosInstance.get(`/project/get/personal`);
    }

    public checkPermission(projectId: string) {
        return axiosInstance.get(`/permission/check`, {
            params: {
                "pid": projectId
            }
        });
    }

    public async getPersonalGroups(projectId: string) {
        return axiosInstance.get(`/groups/get/personal?pid=${projectId}`);
    }

    public async getGroupDetails(projectId: string, groupId: string) {
        return axiosInstance.get('/groups/get/details', {
            params: {
                pid: projectId,
                gid: groupId
            }
        })
    }

    public async addGroup(projectId: string, groupData: any) {
        const groupType = groupData.groupType || "NATURAL"; // Adjust as needed
        return axiosInstance.post(`/groups/add?pid=${projectId}&groupType=${groupType}`, groupData);
    }

    public async updateGroup(projectId: string, groupData: any) {
        const groupType = groupData.groupType || "NATURAL"; // Adjust as needed
        return axiosInstance.post(`/groups/update?pid=${projectId}&groupType=${groupType}`, groupData);
    }

    public async deleteGroup(projectId: string, groupId: string) {
        return axiosInstance.delete(`/groups/delete?pid=${projectId}&gid=${groupId}`);
    }

    public async searchGroups(projectId: string, query: string) {
        return axiosInstance.get(`/groups/search?pid=${projectId}&query=${query}`);
    }

    async getUsersInGroup(groupId: string, projectId: string) {
        return axiosInstance.get(`/groups/getUsers`, {
            params: {
                "pid": projectId,
                "gid": groupId
            }
        })
    }

    async getProfile() {
        return axiosInstance.get(`/auth/profile`);
    }

    async getProjectName(projectId: string) {
        return axiosInstance.get(`/project/get/name`, {
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
        return axiosInstance.get(`/contacts/search/all`, {
            params: {
                query: query,
                projectId: projectId
            }
        });
    }

    async saveOutlookPassword(formData: { email: string; password: string }) {
        return axiosInstance.post(`/auth/save-outlook-password`, formData);
    }

    async checkOutlookAuthorization(): Promise<boolean> {
        try {
            const response = await axiosInstance.get('/auth/check-outlook-authorization');
            return response.data; // Expected response: { isAuthorized: true/false }
        } catch (error) {
            console.error('Outlook-Autorisierungsprüfung fehlgeschlagen:', error);
            throw error;
        }
    }

    async checkOutlookPassword(email: string, password: string): Promise<boolean> {
        try {
            console.log(email, password);
            const response = await axiosInstance.post('/auth/validate-outlook-password', {
                email: email,
                password: password,
            });
            return response.data;
        } catch (error) {
            console.error('Fehler bei der Überprüfung des Outlook-Passworts:', error);
            throw error;
        }
    }


    // Holt ein einzelnes Projekt basierend auf der Projekt-ID
    public getProject(projectId: string) {
        return axios.get(`/api/project/get/single`, {
            params: {
                "pid": projectId
            }
        });
    }

    // Löscht ein Projekt basierend auf der Projekt-ID
    public deleteProject(projectId: string) {
        return axios.delete(`/api/project/delete`, {
            data: projectId
        });
    }

    // Aktualisiert ein Projekt basierend auf den übergebenen Projektdaten
    public updateProject(projectData: any) {
        return axios.put(`/api/project/update`, projectData);
    }
}