<template>
  <div id="mailFormContainer">
    <div id="formHeader">
      <h1>Neue E-Mail</h1>
    </div>
    <div id="formContent">
      <form @submit.prevent="handlePreview" id="mailForm">
        <!-- Empfänger Auswahl -->
        <div class="form-group">
          <label for="recipients" class="form-label">Empfänger:</label>
          <div class="input-container">
            <input
                type="text"
                v-model="searchTerm"
                class="form-input search-input"
                placeholder="Geben Sie hier den Namen oder Gruppen ein..."
                @focus="fetchUsersAndGroups(searchTerm)"
                @input="onSearchInput"
            />
            <ul
                v-if="searchTerm.length > 0 && (filteredUsers.length || filteredGroups.length)"
                class="autocomplete"
            >
              <li v-for="user in filteredUsers" :key="user.id" @click="selectUser(user)">
                {{ user.firstName }} {{ user.lastName }} - {{ user.mailAddress }}
              </li>
              <li v-for="group in filteredGroups" :key="group.id" @click="selectGroup(group)">
                Gruppe: {{ group.name }}
              </li>
              <li v-if="loading">Laden...</li>
            </ul>
          </div>
        </div>

        <!-- Ausgewählte Empfänger anzeigen -->
        <div id="selectedUsersList">
          <div id="selectedRecipients">
            <div class="tag" v-for="user in selectedUsers" :key="user.id">
              {{ user.firstName }} {{ user.lastName }}
              <span class="tag-remove" @click="removeUser(user)">✕</span>
            </div>
            <div class="tag" v-for="group in selectedGroups" :key="group.id">
              Gruppe: {{ group.name }}
              <span class="tag-remove" @click="removeGroup(group)">✕</span>
            </div>
          </div>
        </div>

        <!-- Sendezeit auswählen -->
        <div class="form-group">
          <label for="sendLater" class="form-label">Senden am:</label>
          <div class="input-container flex">
            <input
                type="checkbox"
                id="sendLater"
                v-model="checked"
                class="form-checkbox"
            />
            <label for="sendLater" class="form-checkbox-label">später senden</label>
          </div>
          <div v-if="checked" class="datetime-picker">
            <VueDatePicker v-if="checked"
                           locale="de-AT"
                           v-model="date"
                           class="datepicker"
                           id="datepicker"
                           now-button-label="Current"
                           format="dd-MM-yyyy"
                           :enable-time-picker="false"
                           placeholder='Datum auswählen'
                           :min-date="format(new Date(), 'yyyy-MM-dd')">
            </VueDatePicker>
            <VueDatePicker
                v-model="time"
                class="timepicker"
                time-picker
                placeholder="Uhrzeit auswählen"
            />
          </div>
        </div>

        <!-- Vorlage auswählen -->

        <div class="form-group">
          <label for="template" class="form-label">Vorlage:</label>
          <div id="mailFlexBox">
            <div class="form-group">
              <div id="mailTemplateInput" class="input-container">
                <input
                    type="text"
                    v-model="filter"
                    class="form-input search-input"
                    placeholder="Vorlage suchen..."
                    @input="onTemplateSearch"
                />
                <ul v-if="dropdownVisible" class="autocomplete">
                  <li
                      v-for="template in fetchedTemplates"
                      :key="template.id"
                      v-show="template.visible"
                      @click="selectTemplate(template)"
                  >
                    {{ template.name }}
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </div>

        <!-- Vorschau anzeigen Button -->
        <div class="form-actions">
          <button type="button" @click="handlePreview" :disabled="!canPreview" class="btn btn-outline">
            Vorschau anzeigen
          </button>
        </div>
      </form>

      <!-- Mail Preview Component -->
      <MailPreviewComponent
          v-if="showPreview"
          :selectedTemplate="selectedTemplate"
          :selectedUsers="previewRecipients"
          :personalized="personalized"
          :visible="showPreview"
          :scheduledAt="parseDate()"
          @close="closePreview"
          @send-mail="sendMail"
      />
    </div>
  </div>
</template>

<script setup lang="ts">

import {computed, onMounted, ref, watch} from 'vue';
import {Service} from '@/services/service';
import {useAppStore} from '@/stores/app.store';
import {useRouter} from 'vue-router';
import MailPreviewComponent from '@/components/mail/MailPreviewComponent.vue';
import {format} from 'date-fns';

interface Template {
  id: number;
  name: string;
  headline: string;
  greeting: string;
  content: string;
  visible: boolean;
}

interface User {
  id: string;
  firstName: string;
  lastName: string;
  mailAddress: string;
  displayName: string;
}

interface Group {
  id: string;
  name: string;
}

const router = useRouter();
const appStore = useAppStore();
const filter = ref('');
const dropdownVisible = ref(false);
const selectedTemplate = ref<Template | null>(null);
const receiverInput = ref('');
const checked = ref(false);
const fetchedTemplates = ref<Template[]>([]);

const selectedUsers = ref<User[]>([]);
const selectedGroups = ref<Group[]>([]);
const searchTerm = ref('');
const users = ref<User[]>([]);
const groups = ref<Group[]>([]);
const loading = ref(false);
const personalized = ref(true);
const showPreview = ref(false);

// Neuer Ref für die Vorschau-Empfänger
const previewRecipients = ref<User[]>([]);

const canPreview = computed(() => (selectedUsers.value.length > 0 || selectedGroups.value.length > 0) && selectedTemplate.value);

const handlePreview = async () => {
  // Überprüfe, ob eine Vorlage ausgewählt wurde und mindestens ein Empfänger vorhanden ist
  if (selectedUsers.value.length === 0 && selectedGroups.value.length === 0) {
    alert('Es sind keine Empfänger für die Vorschau vorhanden.');
    return;
  }

  if (!selectedTemplate.value) {
    alert('Bitte wählen Sie eine Vorlage aus.');
    return;
  }

  try {
    // Erstelle eine Kopie der individuell ausgewählten Nutzer
    let combinedUsers: User[] = [...selectedUsers.value];

    // Wenn Gruppen ausgewählt sind, lade ihre Mitglieder
    if (selectedGroups.value.length > 0) {
      // Verwende Promise.all, um alle Gruppenmitglied-Abfragen parallel durchzuführen
      const groupUsersPromises = selectedGroups.value.map(group =>
          Service.getInstance().getUsersInGroup(group.id, appStore.$state.project)
      );

      const groupsResponses = await Promise.all(groupUsersPromises);

      groupsResponses.forEach(response => {
        combinedUsers = combinedUsers.concat(response.data);
      });
    }

    // Entferne doppelte Nutzer basierend auf der Nutzer-ID
    const uniqueUsersMap = new Map<string, User>();
    combinedUsers.forEach(user => {
      uniqueUsersMap.set(user.id, user);
    });
    combinedUsers = Array.from(uniqueUsersMap.values());

    // Setze die Vorschau-Empfänger
    previewRecipients.value = combinedUsers;

    console.log('Preview Recipients:', previewRecipients.value); // Debugging

    if (previewRecipients.value.length > 0 && selectedTemplate.value) {
      showPreview.value = true;
    } else {
      alert('Es sind keine Empfänger für die Vorschau vorhanden.');
    }
  } catch (error) {
    console.error('Fehler beim Laden der Gruppenmitglieder:', error);
    alert('Fehler beim Laden der Gruppenmitglieder.');
  }
};

const closePreview = () => {
  showPreview.value = false;
};

const closeDropdown = () => {
  dropdownVisible.value = false;
};

/* date picker */
const date = ref({
  day: new Date().getDate().toString().padStart(2, "0"),
  month: (new Date().getMonth() + 1).toString().padStart(2, "0"),
  year: new Date().getFullYear()
});

const time = ref({
  hours: new Date().getHours().toString().padStart(2, "0"),
  minutes: new Date().getMinutes().toString().padStart(2, "0"),
});

const scheduledAt = ref({
  ...date.value,
  ...time.value
});

watch(date, (newDate) => {
  console.log("newDate structure:", newDate);
  scheduledAt.value.year = newDate.getFullYear().toString();
  scheduledAt.value.month = (newDate.getMonth() + 1).toString().padStart(2, "0");
  scheduledAt.value.day = newDate.getDate().toString().padStart(2, "0");
});

watch(time, (newTime) => {
  console.log("newTime structure:", newTime);
  scheduledAt.value.hours = newTime.hours.toString().padStart(2, "0");
  scheduledAt.value.minutes = newTime.minutes.toString().padStart(2, "0");
});

const parseDate = () => {
  if (checked.value) {
    return `${scheduledAt.value.year}-${scheduledAt.value.month}-${scheduledAt.value.day}T${scheduledAt.value.hours}:${scheduledAt.value.minutes}:00.000Z`;
  } else {
    return null;
  }
}

/**/

onMounted(() => {
  getTemplates();

  document.addEventListener('click', handleClickOutside);
});

const handleClickOutside = (event: MouseEvent) => {
  const target = event.target as HTMLElement;
  const mailTemplateInput = document.getElementById('mailTemplateInput');
  if (mailTemplateInput && !mailTemplateInput.contains(target)) {
    closeDropdown();
  }
};

const onSearchInput = () => {
  if (searchTerm.value.length > 0) {
    fetchUsersAndGroups(searchTerm.value);
  } else {
    users.value = [];
    groups.value = [];
  }
};

const onTemplateSearch = () => {
  filterFunction();
  dropdownVisible.value = filter.value.length > 0;
};

const filterFunction = () => {
  const filterValue = filter.value.trim().toLowerCase();
  fetchedTemplates.value.forEach(item => {
    item.visible = item.name.toLowerCase().includes(filterValue);
  });
};

const showDropdown = () => {
  dropdownVisible.value = filter.value.length > 0;
};

const selectTemplate = (template: Template) => {
  selectedTemplate.value = template;
  filter.value = template.name;
  setTimeout(() => {
    closeDropdown();
  }, 0);
};

const getTemplates = async () => {
  try {
    const response = await Service.getInstance().getTemplates(appStore.$state.project);
    fetchedTemplates.value = response.data.map((template: any) => ({
      ...template,
      visible: true
    }));
    console.log('Fetched Templates:', fetchedTemplates.value); // Debugging
  } catch (error) {
    console.error('Error fetching templates:', error);
  }
};

const sortSelectedUsers = (selectedUsers: User[]): string[] => {
  return selectedUsers.map(user => user.id).sort();
};

const sendMail = async () => {
  try {
    const mailForm = {
      receiver: {
        contacts: sortSelectedUsers(selectedUsers.value),  // Individuelle Nutzer
        groups: selectedGroups.value.map(group => parseInt(group.id)) // Gruppen
      },
      templateId: selectedTemplate.value?.id,
      personalized: personalized.value,
      scheduledAt: parseDate()
    };

    console.log(mailForm)
    const response = await Service.getInstance().sendEmails(mailForm, appStore.$state.project);
    console.log('Erfolgreich gesendet:', response.data);

    selectedUsers.value = [];
    selectedGroups.value = [];

    router.push({name: 'mail', query: {mailsend: 'true'}});
  } catch (error) {
    console.error('Fehler beim Senden der E-Mail:', error);
    alert('Fehler beim Senden der E-Mail.');
  }
};

const fetchUsersAndGroups = async (query: string) => {
  loading.value = true;
  try {
    const usersResponse = await Service.getInstance().searchContacts(query);
    users.value = usersResponse.data;

    const groupsResponse = await Service.getInstance().searchGroups(appStore.$state.project, query);
    groups.value = groupsResponse.data;
  } catch (error) {
    console.error('Fehler beim Abrufen von Benutzern oder Gruppen:', error);
    // Optional: Setze eine Fehlermeldung, falls erforderlich
  } finally {
    loading.value = false;
  }
};

watch(searchTerm, (newTerm) => {
  if (newTerm.length > 0) {
    fetchUsersAndGroups(newTerm);
  } else {
    users.value = [];
    groups.value = [];
  }
});

const filteredUsers = computed(() => users.value);
const filteredGroups = computed(() => groups.value);

const selectGroup = (group: Group) => {
  if (!selectedGroups.value.find(g => g.id === group.id)) {
    selectedGroups.value.push(group);
  }

  searchTerm.value = '';
  users.value = [];
  groups.value = [];
};

const handleSubmit = () => {
  sendMail();
};

watch(searchTerm, (newTerm) => {
  if (newTerm.length > 0) {
    fetchUsersAndGroups(newTerm);
  } else {
    users.value = [];
    groups.value = [];
  }
});

const selectUser = (user: User) => {
  if (!selectedUsers.value.find(u => u.id === user.id)) {
    selectedUsers.value.push(user);
  }
  searchTerm.value = '';
  users.value = [];
  groups.value = [];
};

const removeUser = (user: User) => {
  selectedUsers.value = selectedUsers.value.filter(u => u.id !== user.id);
};

const removeGroup = (group: Group) => {
  selectedGroups.value = selectedGroups.value.filter(g => g.id !== group.id);
};
</script>

<style scoped>
/* Container */
#mailFormContainer {
  width: 86.5%;
  margin-top: 2%;
  margin-lexft: 1.5%;
  display: flex;
  flex-direction: column;
}

#mailFlexBox {
  display: flex;
  flex-direction: row;
  width: 100%;
}

#mailTemplateInput {
  width: 32vw;
}

#personalizedBox {
  padding-left: 1%;
  width: 100%;
}

/* Header */
#formContent {
  height: 85%;
  margin-top: 2%;
  background-color: white;
  box-shadow: 5px 5px 10px lightgray;
  padding: 20px;
}

#mailForm {
  padding: 2% 3%;
}

#formHeader {
  display: flex;
  flex-direction: row;
  background-color: white;
  box-shadow: 0 4px 4px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.12);
}

#formHeader h1 {
  margin-left: 4%;
  margin-top: 2%;
  margin-bottom: 2%;
  font-size: 1.1em;
}

.form-group {
  margin-bottom: 20px;
}

.form-label {
  color: #555;
  margin-bottom: 8px;
  display: block;
}

.input-container {
  position: relative;
  width: 100%;
}

.form-input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 0.9rem;
  transition: border-color 0.2s ease-in-out;
}

.form-input:focus {
  border-color: #007bff;
}

.search-input {
  background-color: #f9f9f9;
}

/* Tags für ausgewählte Benutzer/Gruppen */
#selectedUsersList {
  width: 100%;
  height: auto;
  border: 1px solid #ccc;
  padding: 10px;
  overflow-y: auto;
}

#selectedRecipients {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5%;
  margin-bottom: 20px;
}

.tag {
  background-color: lightblue;
  color: white;
  border-radius: 20px;
  padding: 6px 12px;
  font-size: 0.8rem;
  display: inline-flex;
  align-items: center;
}

.tag-remove {
  margin-left: 8px;
  cursor: pointer;
}

/* Checkbox */
.flex {
  display: flex;
  align-items: center;
  gap: 10px;
}

.form-checkbox {
  transform: scale(1.3);
}

.form-checkbox-label {
  font-size: 1rem;
  color: #333;
}

.datetime-picker {
  display: flex;
  gap: 20px;
  margin-top: 10px;
}

.datepicker,
.timepicker {
  width: 45%;
}

.autocomplete {
  list-style-type: none;
  margin: 5px 0 0;
  padding: 0;
  background-color: white;
  border: 1px solid #ccc;
  border-radius: 6px;
  position: absolute;
  width: 100%;
  max-height: 200px;
  overflow-y: auto;
  z-index: 1000;
}

.autocomplete li {
  padding: 12px;
  cursor: pointer;
  transition: background-color 0.2s ease-in-out;
}

.autocomplete li:hover {
  background-color: #007bff;
  color: white;
}

/* Form Actions */
.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 30px;
}

.btn {
  padding: 12px 20px;
  font-size: 1rem;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s ease-in-out;
}

.btn-outline {
  background-color: transparent;
  color: #007bff;
  border: 2px solid #007bff;
  margin-right: 10px;
}

.btn-outline:hover {
  background-color: #007bff;
  color: white;
}

.btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}
</style>