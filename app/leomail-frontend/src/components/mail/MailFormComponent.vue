<template>
  <div id="mailFormContainer">
    <div id="formHeader">
      <h1>Neue E-Mail</h1>
    </div>
    <div id="formContent">
      <form @submit.prevent="handlePreview" id="mailForm">
        <!-- Absender Auswahl -->
        <div class="form-group">
          <label for="senderMail" class="form-label">Senden als: </label>
          <div class="input-with-icon">
            <select v-model="selectedSender" class="form-input">
              <option :value="personalMail">{{profileData.firstName}} {{profileData.lastName}} <{{ personalMail }}></option>
              <option :value="projectMail.id">{{ projectMail.displayName}} <{{ projectMail.mailAddress }}></option>
            </select>
            <i class="fas fa-chevron-down"></i>
          </div>
        </div>

        <!-- Empfänger Auswahl -->
        <div class="form-group">
          <label for="recipients" class="form-label">Empfänger:</label>
          <div class="input-container-recipients">
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
          <div id="flexSendLaterContainer">
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
                             format="dd.MM.yyyy"
                             :enable-time-picker="false"
                             placeholder='Datum auswählen'
                             :min-date="new Date()">
              </VueDatePicker>
              <VueDatePicker
                  v-model="time"
                  class="timepicker"
                  time-picker
                  placeholder="Uhrzeit auswählen"
              />
            </div>
          </div>
        </div>

        <!-- Vorlage auswählen -->
        <div class="form-group">
          <label for="template" class="form-label">Vorlage:</label>
          <div id="mailFlexBox">
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

        <!-- Vorschau anzeigen Button -->
        <div class="form-actions">
          <button type="button" @click="handlePreview" :disabled="!canPreview" class="btn">
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
          :scheduledAt="isScheduled()"
          @close="closePreview"
          @send-mail="sendMail"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { Service } from '@/services/service';
import { useAppStore } from '@/stores/app.store';
import { useRouter } from 'vue-router';
import MailPreviewComponent from '@/components/mail/MailPreviewComponent.vue';
import { format } from 'date-fns';

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

// Neue Referenzen für Absender-Mails
const profileData = ref({
  firstName: '',
  lastName: '',
  mailAddress: '',
  schoolClass: '',
  departement: ''
});
const personalMail = ref();
const projectMail = ref({ id: '', mailAddress: '', displayName: ''});
const selectedSender = ref();

const getProfile = async () => {
  const response = await Service.getInstance().getProfile().then(response => {
    profileData.value = response.data;
    personalMail.value = profileData.value.mailAddress;
    selectedSender.value = profileData.value.mailAddress;
  });
};

const fetchProjectMail = async () => {
  const response = await Service.getInstance().fetchProjectMailData(appStore.$state.project);
  projectMail.value = response.data;
};

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

/* date picker */
const date = ref<Date | null>(null);

const time = ref({
  hours: new Date().getHours().toString().padStart(2, "0"),
  minutes: new Date().getMinutes().toString().padStart(2, "0")
});

const parseDate = () => {
  if (!date.value) {
    return null;
  }

  const d = date.value;
  console.log("d" + d)
  const day = String(d.getDate()).padStart(2, '0');
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const year = d.getFullYear();

  const hours = String(time.value.hours).padStart(2, '0');
  const minutes = String(time.value.minutes).padStart(2, '0');

  console.log(`${year}-${month}-${day}T${hours}:${minutes}:00.000Z`);
  return `${year}-${month}-${day}T${hours}:${minutes}:00.000Z`;
};

const isScheduled = () => {
  if(checked.value == true) {
    return parseDate()
  } else{
    /*const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');

    return `${year}-${month}-${day}T${hours}:${minutes}:00.000Z`;*/
    return null;
  }
};

/**/

onMounted(() => {
  getProfile();
  getTemplates();
  fetchProjectMail();

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

const closeDropdown = () => {
  dropdownVisible.value = false;
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
        contacts: sortSelectedUsers(selectedUsers.value),
        groups: selectedGroups.value.map(group => parseInt(group.id))
      },
      templateId: selectedTemplate.value?.id,
      personalized: personalized.value,
      scheduledAt: isScheduled(),
      from: {
        mailType: selectedSender.value === personalMail.value ? 'PERSONAL' : 'PROJECT',
        id: selectedSender.value === personalMail.value ? '' : appStore.$state.project
      }
    };

    console.log(mailForm);
    const response = await Service.getInstance().sendEmails(mailForm, appStore.$state.project);

    selectedUsers.value = [];
    selectedGroups.value = [];

    router.push({ name: 'mail', query: { mailsend: 'true' } });
  } catch (error) {
    console.error('Fehler beim Senden der E-Mail:', error);
    alert('Fehler beim Senden der E-Mail.');
  }
  }

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
.input-with-icon {
  position: relative;
}

.fas.fa-chevron-down {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  color: #BEBEBE;
}

#flexSendLaterContainer {
  display: flex;
  flex-direction: row;
}

/* Container */
#mailFormContainer {
  width: 86.5%;
  margin-top: 2%;
  margin-left: 1.5%;
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
}

#mailForm {
  padding: 0 3%;
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
  margin-bottom: 1%;
  margin-top: 2%;
}

.form-label {
  color: #555;
  display: block;
}

.input-container {
  position: relative;
  width: 15%;
}

.form-input {
  all: unset;
  border: solid 1px #BEBEBE;
  border-radius: 5px;
  padding: 0.6vw;
  width: 100%;
  font-size: 0.8em;
}

.form-input:focus {
  border-color: #007bff;
}

/* Tags für ausgewählte Benutzer/Gruppen */
#selectedUsersList {
  width: 100%;
  height: 15vh;
  border: 1px solid #ccc;
  padding: 10px;
  border-radius: 10px;
  overflow-y: scroll;
}

#selectedRecipients {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5%;
  margin-bottom: 20px;
}

.tag {
  background-color: rgba(0, 123, 255, 0.45);
  color: white;
  border-radius: 10px;
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
  width: 38%;
}

/* Anpassung des Autocomplete-Containers */
.input-container-recipients {
  width: 30vw; /* Hier definierst du die Breite des Input-Felds */
  position: relative; /* Um sicherzustellen, dass das Autocomplete innerhalb des Containers positioniert wird */
}

.autocomplete {
  list-style-type: none;
  padding: 0;
  margin: 0;
  background-color: white;
  border: solid 1px #D1D1D1;
  border-top: none;
  position: absolute;
  top: 100%; /* Platzierung direkt unter dem Input-Feld */
  left: 0;
  width: 100%; /* Die Breite der Autocomplete-Liste an das Input-Feld anpassen */
  z-index: 1000;
  max-height: 200px;
  overflow-y: auto; /* Scrollbar bei zu vielen Ergebnissen */
  border-radius: 0 0 8px 8px;
}

.autocomplete li {
  padding: 12px;
  cursor: pointer;
  transition: background-color 0.2s ease-in-out;
}

.autocomplete li:hover {
  background-color: rgba(0, 123, 255, 0.36);
  color: white;
}

/* Form Actions */
.form-actions {
  display: flex;
  margin-top: 2%;
  justify-content: flex-end;
}

.btn {
  background-color: #78A6FF;
  color: white;
  padding: 12px 20px;
  border: none;
  border-radius: 5px;
  font-size: 1em;
  cursor: pointer;
  align-self: center;
}

.btn:hover {
  background-color: rgba(75, 129, 253, 0.86);
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2);
}

.btn:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}
</style>