import { defineStore } from 'pinia';

export const appStore = defineStore('project', {
    state: () => ({
        project: String
    }),
    persist: true
});
