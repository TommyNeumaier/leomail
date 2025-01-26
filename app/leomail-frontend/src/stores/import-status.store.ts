import { defineStore } from 'pinia';
import { ref } from 'vue';
import { Service } from '@/services/service';

export const useImportStatusStore = defineStore('importStatus', () => {
    const importing = ref(true);

    /**
     * Aktualisiert den Importstatus.
     */
    const updateImportStatus = async () => {
        try {
            const response = await Service.getInstance().getImportStatus();
            importing.value = response.importing;
        } catch (error) {
            console.error('Fehler beim Aktualisieren des Importstatus:', error);
            importing.value = false;
        }
    };

    return { importing, updateImportStatus };
});
