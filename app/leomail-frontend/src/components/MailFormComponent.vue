<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, type Ref, ref, watch } from "vue";
import { Quill } from "@vueup/vue-quill";
import { Service } from "@/services/service";
import { useAppStore } from "@/stores/app.store";
import axios from "axios";
import { useRouter } from 'vue-router';
import {format} from "date-fns";

const router = useRouter();

interface Template {
  id: number;
  name: string;
  headline: string;
  greeting: string;
  content: string;
  visible: boolean;
}

interface User {
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
const personalized = ref(false);

const formState = ref({
  name: '',
  description: '',
  mailAddress: '',
  password: '',
  members: selectedUsers.value
});

const errors = ref({
  name: '',
  description: '',
  email: '',
  password: '',
  selectedUsers: ''
});

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
  scheduledAt.value.year = newDate.getFullYear().toString();
  scheduledAt.value.month = (newDate.getMonth() + 1).toString().padStart(2, "0");
  scheduledAt.value.day = newDate.getDate().toString().padStart(2, "0");
});

watch(time, (newTime) => {
  scheduledAt.value.hours = newTime.getHours().toString().padStart(2, "0");
  scheduledAt.value.minutes = newTime.getMinutes().toString().padStart(2, "0")
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

const parseReceiverInput = () => {
  receiver.value = receiverInput.value.split(',').map(Number);
};

const parseDate = () => {
  if (checked.value) {
    return '${scheduledAt.value.year}-${scheduledAt.value.month}-${scheduledAt.value.day}T${scheduledAt.value.hours}:${scheduledAt.value.minutes}:00.000Z';
  } else {
    return null;
  }
}
const sortSelectedUsers = (selectedUsers: User[]): number[] => {
  return selectedUsers.map(user => user.id).sort((a, b) => a - b);
}
const sendMail = async () => {
  try {
    parseReceiverInput();
    console.log(receiver);
    const mailForm = {
      receiver: {
        contacts: sortSelectedUsers(selectedUsers.value),
        groups: selectedGroups.value.map(group => group.id)
      },
      templateId: selectedTemplate.value?.id,
      personalized: personalized.value,  //todo: must be optimized if bauer wants the checkbox
      scheduledAt: parseDate()
    };
    console.log(mailForm);
    console.log(parseDate());
    const response = await Service.getInstance().sendEmails(mailForm, appStore.$state.project);
    console.log('Erfolgreich gesendet:', response.data);
    router.push({ name: 'mail', query: { mailsend: 'true' } });
  } catch (error) {
    console.error('Fehler beim Senden der Daten:', error);
  }
  selectedUsers.value = [];
  selectedGroups.value = [];
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

const selectGroup = (group: Group) => {
  if (!selectedGroups.value.find(g => g.id === group.id)) {
    selectedGroups.value.push(group);
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

<template>
  <div id="bigContainer">
    <div id="VGHeaderBox">
      <h1 id="vgHeading">Neue Email</h1>
    </div>
    <div id="formBox">
      <form>
        <div id="userBox">
          <div class="boxLabel">
            <label for="users" class="mail-label">An:</label><br>
          </div>
          <div class="multiselect">
            <input type="text" v-model="searchTerm" class="mailForm" placeholder="Benutzer oder Gruppen suchen">
            <ul v-if="searchTerm.length > 0 && (filteredUsers.length || filteredGroups.length)">
              <!-- Display users -->
              <li v-for="user in filteredUsers" :key="user.id" @click="selectUser(user)">
                <div class="user-info">
                  <span>{{ user.firstName }} {{ user.lastName }}</span>
                  <small>{{ user.mailAddress }}</small>
                </div>
              </li>
              <!-- Display groups -->
              <li v-for="group in filteredGroups" :key="group.id" @click="selectGroup(group)">
                <div class="group-info">
                  <span>{{ group.name }}</span>
                </div>
              </li>
              <li v-if="loading">Laden...</li>
            </ul>
          </div>

          <div id="selectedUserBox">
            <div class="selected" v-for="user in selectedUsers" :key="user.id">
              {{ user.firstName }} {{ user.lastName }} <span class="remove" @click="removeUser(user)"></span>
            </div>
            <div class="selected" v-for="group in selectedGroups" :key="group.id">
              Gruppe: {{ group.name }} <span class="remove" @click="removeGroup(group)"></span>
            </div>
          </div>
        </div>
        <span class="error">{{ errors.selectedUsers }}</span>

        <div id="formFlexBox">
          <div id="dateFlexBox">
            <div class="boxLabel">
              <label class="mail-label">Senden am:</label></div>
            <br>
            <div id="checkboxSendLater">
              <input type="checkbox" id="date" v-model="checked">
              <label for="date">später senden</label>
            </div>
            <div id="datepickerFlexBox">
              <VueDatePicker v-if="checked" locale="de-AT" v-model="date" class="datepicker" id="datepicker"
                             now-button-label="Current" format="dd-mm-yyyy" :enable-time-picker="false"
                             placeholder='Date' date-picker
                             :min-date="format(new Date(), 'yyyy-MM-dd')"></VueDatePicker>
              <!--https://vue3datepicker.com/props/localization/-->
              <VueDatePicker v-if="checked" v-model="time" class="datepicker" time-picker placeholder='Time'
                             :min-time="format(new Date(), 'HH:mm')"/>
            </div>
          </div>
        </div>

        <div id="flexBoxContainer">
          <div id="templateBox">
            <div class="boxLabel">
              <label for="template" class="mail-label">Vorlagen:</label></div>
            <br>
            <div id="searchSelectBox">
              <div>
                <input type="text" id="myInput" v-model="filter" placeholder="Search.." @input="filterFunction"
                       @focus="showDropdown" class="mailForm">
              </div>
              <div v-if="dropdownVisible" class="dropdown-content">
                <a v-for="item in fetchedTemplates" :key="item.id" v-show="item.visible"
                   @click="selectTemplate(item)">{{ item.name }}</a>
              </div>
            </div>
          </div>
          <div id="checkBoxContainer">
            <input type="checkbox" v-model="personalized">
            <label>personalisieren</label>
          </div>
        </div>

        <div class="editor-wrapper">
          <div id="editor" class="quill-editor"></div>
        </div>
        <button type="button" @click="handleSubmit">Absenden</button>
      </form>
    </div>
  </div>
</template>

<style scoped>
#selectedUserBox {
  width: 50vw;
  margin-left: 2%;
  height: 12vh;
  border: black solid 1px;
  overflow-y: scroll;
}

#userBox {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-info small {
  color: #B3B3B3;
}

li:hover {
  background-color: rgba(75, 129, 253, 0.86);
  color: white;
}

li:hover .user-info small {
  color: white;
}

.multiselect {
  display: block;
  border: solid 1px #BEBEBE;
  border-radius: 5px;
  width: 25vw;
  font-size: 0.5em;
}

.multiselect::placeholder {
  color: #B3B3B3;
}

.multiselect:focus {
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2);
}

.selected {
  display: inline-flex;
  align-items: center;
  background-color: blue;
  color: white;
  border-radius: 3px;
  padding: 0.2vh 0.3vw;
  margin-right: 0.5vw;
  margin-bottom: 0.1vh;
  font-size: 0.5rem;
}

.remove {
  cursor: pointer;
  margin-left: 5px;
}

.remove:after {
  content: '×';
  color: white;
  font-weight: bold;
}

.multiselect input[type="text"] {
  all: unset;
  width: 100%;
  padding: 0.6vw;
  box-sizing: border-box;
  border-radius: 5px;
}

.multiselect input:focus {
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2);
}

ul {
  list-style-type: none;
  padding: 0;
  margin: 0;
  background-color: #FFF;
  border: solid 1px #BEBEBE;
  border-top: none;
  position: absolute;
  width: 100%;
  z-index: 1000;
}

li {
  padding: 10px;
  cursor: pointer;
}

.error {
  color: red;
  font-size: 0.7em;
}

#checkBoxContainer {
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
  margin-left: 5%;
}

#checkBoxContainer label {
  padding-left: 1vw;
}

#formFlexBox {
  width: 50%;
}

#flexBoxContainer {
  display: flex;
  flex-direction: row;
  width: 100%;
}

#bigContainer {
  width: 86.5%;
  margin-top: 2%;
  margin-left: 1.5%;
  display: flex;
  flex-direction: column;
}

#date {
  margin-right: 2vw;
  margin-left: 1vw;
}

.boxLabel {
  width: 6vw;
}

#checkboxSendLater {
  width: 15vw;
}

#datepickerFlexBox {
  width: 50%;
  display: flex;
  flex-direction: row;
}

.datepicker {
  width: 8vw; /* Hier können Sie die gewünschte Schriftgröße angeben */
}

#templateBox {
  display: flex;
  flex-direction: row;
}

#receiverFlexBox {
  display: flex;
  flex-direction: row;
}

#dateFlexBox {
  display: flex;
  flex-direction: row;
  height: 5vh;
  margin-top: 1vh;
  margin-bottom: 1vh;
  align-items: center;
}

form {
  padding: 1% 3%;
}

.mail-label {
  color: #5A5A5A;
  font-size: 0.8em;
}

.mailForm::placeholder {
  color: #B3B3B3;
}

.mailForm {
  display: block;
  all: unset;
  border: solid 1px #BEBEBE;
  border-radius: 5px;
  font-size: 0.5em;
  width: 100%;
  padding: 0.6vw;
}

.mailForm:focus {
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2);
}

body {
  width: 100%;
}

#formBox {
  background-color: white;
  height: 87%;
  margin-top: 2vh;
  box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.12);
}

#VGHeaderBox {
  display: flex;
  flex-direction: row;
  background-color: white;
  box-shadow: 0 4px 4px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.12);
}

#vgHeading {
  margin-left: 4%;
  margin-top: 2%;
  margin-bottom: 2%;
  font-size: 1.1em;
}

#searchSelectBox {
  display: flex;
  flex-direction: column;
  width: 25vw;
}

#myInput {
  width: 100%;
}

/* Dropdown Content (Hidden by Default) */
.dropdown-content {
  width: 100%;
  border: 1px solid #ddd;
  z-index: 1;
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2);
  max-height: 25vh;
  overflow-y: scroll;
  cursor: pointer;
}

/* Links inside the dropdown */
.dropdown-content a {
  color: black;
  padding: 2% 4%;
  font-size: 0.8rem;
  text-decoration: none;
  display: block;
}

/* Change color of dropdown links on hover */
.dropdown-content a:hover {
  background-color: #f1f1f1
}

.editor-wrapper {
  border: black solid 1px;
  height: 90%;
}
</style>