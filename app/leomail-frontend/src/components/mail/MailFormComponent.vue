<template>
  <div id="mailFormContainer">
    <div id="formHeader">
      <h1>Neue E-Mail</h1>
    </div>
    <div id="formContent">
      <form @submit.prevent="handlePreview" id="mailForm">
        <div class="form-group">
          <label for="recipients" class="form-label">Empfänger:</label>
          <div class="input-container">
            <input
                type="text"
                v-model="searchTerm"
                class="form-input search-input"
                placeholder="Geben Sie hier den Namen oder Gruppen ein..."
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

        <div id="selectedUsersList">
          <div id="selectedRecipients">
            <div class="tag" v-for="user in selectedUsers" :key="user.id">
              {{ user.firstName }} {{ user.lastName }} <span class="tag-remove" @click="removeUser(user)">✕</span>
            </div>
            <div class="tag" v-for="group in selectedGroups" :key="group.id">
              Gruppe: {{ group.name }} <span class="tag-remove" @click="removeGroup(group)">✕</span>
            </div>
          </div>
        </div>

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

        <label for="template" class="form-label">Vorlage</label>
        <div id="mailFlexBox">
        <div class="form-group">
            <div id="mailTemplateInput" class="input-container">
              <input
                  type="text"
                  v-model="filter"
                  class="form-input search-input"
                  placeholder="Vorlage suchen..."
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

          <!--<div class="form-group flex" id="personalizedBox">
            <input type="checkbox" v-model="personalized" class="form-checkbox"/>
            <label class="form-checkbox-label">E-Mail personalisieren</label>
          </div>-->
        </div>

        <div class="form-actions">
          <button type="button" @click="handlePreview" :disabled="!canPreview" class="btn btn-outline">
            Vorschau anzeigen
          </button>
        </div>
      </form>
      <MailPreviewComponent
          v-if="showPreview"
          :selectedTemplate="selectedTemplate"
          :selectedUsers="selectedUsers"
          :personalized="personalized"
          :visible="showPreview"
          @close="closePreview"
          @send-mail="sendMail"
      />
    </div>
  </div>
</template>

<style scoped>
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

#personalizedBox{
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
  width: 40%;
}

.form-input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 0.7rem;
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
  width: 80vw;
  height: 10vh;
  border: 1px solid #ccc;
}

#selectedRecipients {
  display: flex;
  flex-wrap: wrap;
  gap: 0.2%;
  margin-bottom: 20px;
}

.tag {
  background-color: lightblue;
  color: white;
  border-radius: 20px;
  padding: 6px 12px;
  font-size: 0.7rem;
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
  width: 20%;
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
<script setup lang="ts">
import {computed, nextTick, onMounted, onUnmounted, type Ref, ref, watch} from "vue";
import {Quill} from "@vueup/vue-quill";
import {Service} from "@/services/service";
import {useAppStore} from "@/stores/app.store";
import axios from "axios";
import {useRouter} from 'vue-router';
import {format} from "date-fns";
import MailPreviewComponent from "@/components/mail/MailPreviewComponent.vue";

const router = useRouter();

export interface Template {
  id: number;
  name: string;
  headline: string;
  greeting: string;
  content: string;
  visible: boolean;
}

export interface User {
  id: number;
  firstName: string;
  lastName: string;
  mailAddress: string;
}

interface Group {
  id: number;
  name: string;
}

const appStore = useAppStore();
const filter = ref('');
const dropdownVisible = ref(false);
const selectedTemplate = ref<Template | null>(null);
const receiverInput = ref('');
const receiver = ref<number[]>([]);
const checked = ref(false);
const fetchedTemplates = ref<Template[]>([]);
const editor = ref<Quill | null>(null);

const selectedUsers = ref<User[]>([]) as Ref<User[]>;
const selectedGroups = ref<Group[]>([]) as Ref<Group[]>;
const searchTerm = ref('');
const users = ref<User[]>([]) as Ref<User[]>;
const groups = ref<Group[]>([]) as Ref<Group[]>;
const loading = ref(false);
const personalized = ref(true);
const showPreview = ref(false);

const canPreview = computed(() => selectedUsers.value.length > 0 && selectedTemplate.value);

const handlePreview = () => {
  if (canPreview.value) {
    showPreview.value = true;
  }
};

const closePreview = () => {
  showPreview.value = false;
};

const closeDropdown = () => {
  dropdownVisible.value = false;
};

onMounted(() => {
  getTemplates();
  editor.value = new Quill('#editor');
  nextTick(() => {
    if (selectedTemplate.value) {
      onEditorReady(editor.value);
    }
  });

  document.addEventListener('click', handleClickOutside);
});

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside);
});

const handleClickOutside = (event: MouseEvent) => {
  const target = event.target as HTMLElement;
  const searchSelectBox = document.getElementById('searchSelectBox');
  if (searchSelectBox && !searchSelectBox.contains(target)) {
    closeDropdown();
  }
};

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


const filterFunction = () => {
  const filterValue = filter.value.trim().toLowerCase();
  fetchedTemplates.value.forEach(item => {
    item.visible = item.name.toLowerCase().includes(filterValue);
  });
};

watch(filter, () => {
  filterFunction();
  dropdownVisible.value = filter.value.length > 0;
});

const showDropdown = () => {
  dropdownVisible.value = filter.value.length > 0;
};

const onEditorReady = (editor: Quill) => {
  if (selectedTemplate.value) {
    console.log(selectedTemplate.value.content);
    editor.root.innerHTML = selectedTemplate.value.content;
  }
};

const selectTemplate = (template: Template) => {
  selectedTemplate.value = template;
  filter.value = template.name;
  setTimeout(() => {
    closeDropdown();
  }, 0);

  if (editor.value) {
    nextTick(() => {
      onEditorReady(editor.value);
    });
  }
};

const getTemplates = async () => {
  const response = await Service.getInstance().getTemplates(appStore.$state.project);
  fetchedTemplates.value = response.data.map((template: {
    id: number;
    name: string;
    headline: string;
    greeting: string;
    content: string
  }) => ({
    ...template,
    visible: true
  }));
  console.log(fetchedTemplates.value);
};

const parseDate = () => {
  if (checked.value) {
    return `${scheduledAt.value.year}-${scheduledAt.value.month}-${scheduledAt.value.day}T${scheduledAt.value.hours}:${scheduledAt.value.minutes}:00.000Z`;
  } else {
    return null;
  }
}

const sortSelectedUsers = (selectedUsers: User[]): number[] => {
  return selectedUsers.map(user => user.id).sort((a, b) => a - b);
}
const sendMail = async () => {
  try {
    const mailForm = {
      receiver: {
        contacts: sortSelectedUsers(selectedUsers.value),
        groups: selectedGroups.value.map(group => group.id)
      },
      templateId: selectedTemplate.value?.id,
      personalized: personalized.value,
      scheduledAt: parseDate()
    };

    const response = await Service.getInstance().sendEmails(mailForm, appStore.$state.project);
    console.log('Erfolgreich gesendet:', response.data);

    selectedUsers.value = [];
    selectedGroups.value = [];

    router.push({name: 'mail', query: {mailsend: 'true'}});

  } catch (error) {
    console.error('Fehler beim Senden der Daten:', error);
  }
};
const handleSubmit = () => {
  sendMail();
}

const fetchUsersAndGroups = async (query: string) => {
  loading.value = true;
  try {
    const usersResponse = await Service.getInstance().searchContacts(query)
    users.value = usersResponse.data;

    const groupsResponse = await axios.get(`/api/groups/search?query=${query}&pid=${appStore.$state.project}`);
    groups.value = groupsResponse.data;
  } catch (error) {
    console.error('Error fetching users or groups:', error);
    users.value = [];
    groups.value = [];
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

const selectUser = (user: User) => {
  if (!selectedUsers.value.find(u => u.id === user.id)) {
    selectedUsers.value.push(user);
  }
  searchTerm.value = '';
  users.value = [];
  groups.value = [];
};

const selectGroup = async (group: Group) => {
  if (!selectedGroups.value.find(g => g.id === group.id)) {
    selectedGroups.value.push(group);

    try {
      const response = await Service.getInstance().getUsersInGroup(group.id, appStore.$state.project);
      const usersInGroup = response.data;

      usersInGroup.forEach((user: User) => {
        if (!selectedUsers.value.some(u => u.id === user.id)) {
          selectedUsers.value.push(user);
        }
      });

      console.log("Benutzer in Gruppe:", usersInGroup);

    } catch (error) {
      console.error("Fehler beim Abrufen der Benutzer in der Gruppe:", error);
    }
  }
};


const removeUser = (user: User) => {
  selectedUsers.value = selectedUsers.value.filter(u => u.id !== user.id);
};

const removeGroup = (group: Group) => {
  selectedGroups.value = selectedGroups.value.filter(g => g.id !== group.id);
};</script>