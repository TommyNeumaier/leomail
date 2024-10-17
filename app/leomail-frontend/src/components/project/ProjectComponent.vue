<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import {Service} from "@/services/service";

const router = useRouter();

export interface User {
  id: number;
  firstName: string;
  lastName: string;
  mailAddress: string;
}

const selectedUsers = ref<User[]>([]);
const searchTerm = ref('');
const users = ref<User[]>([]);
const loading = ref(false);

const fetchUsers = async (query: string) => {
  loading.value = true;
  try {
    const usersResponse = await Service.getInstance().searchContacts(query);
    users.value = usersResponse.data;
  } catch (error) {
    console.error('Error fetching users:', error);
    users.value = [];
  } finally {
    loading.value = false;
  }
};

watch(searchTerm, (newTerm) => {
  if (newTerm.length > 0) {
    fetchUsers(newTerm);
  } else {
    users.value = [];
  }
});

const filteredUsers = computed(() => users.value);

const selectUser = (user: User) => {
  if (!selectedUsers.value.find(u => u.id === user.id)) {
    selectedUsers.value.push(user);
  }
  searchTerm.value = '';
  users.value = [];
};

const removeUser = (user: User) => {
  selectedUsers.value = selectedUsers.value.filter(u => u.id !== user.id);
};

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

const validateForm = () => {
  let valid = true;

  if (!formState.value.name) {
    errors.value.name = 'Name ist erforderlich';
    valid = false;
  } else if (formState.value.name.length > 40) {
    errors.value.name = 'Name darf maximal 40 Zeichen lang sein';
    valid = false;
  } else {
    errors.value.name = '';
  }

  if (!formState.value.description) {
    errors.value.description = 'Kurzbeschreibung ist erforderlich';
    valid = false;
  } else if (formState.value.description.length > 240) {
    errors.value.description = 'Kurzbeschreibung darf maximal 240 Zeichen lang sein';
    valid = false;
  } else {
    errors.value.description = '';
  }

  const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!formState.value.mailAddress) {
    errors.value.email = 'E-Mail-Adresse ist erforderlich';
    valid = false;
  } else if (!emailPattern.test(formState.value.mailAddress)) {
    errors.value.email = 'Ungültige E-Mail-Adresse';
    valid = false;
  } else {
    errors.value.email = '';
  }

  if (!formState.value.password) {
    errors.value.password = 'Passwort ist erforderlich';
    valid = false;
  } else {
    errors.value.password = '';
  }

  if (selectedUsers.value.length === 0) {
    errors.value.selectedUsers = 'Mindestens ein Benutzer muss ausgewählt sein';
    valid = false;
  } else {
    errors.value.selectedUsers = '';
  }

  return valid;
};

const handleSubmit = () => {
  if (validateForm()) {
    console.log('Form submitted:', formState.value, selectedUsers.value);
    axios.post("/api/project/add", {
      name: formState.value.name,
      description: formState.value.description,
      mailInformation: {
        mailAddress: formState.value.mailAddress,
        password: formState.value.password
      },
      members: selectedUsers.value,
    }).then(() => {
      router.push({name: 'projects'});
    }).catch((error) => {
      console.error('Error creating project:', error);
    });

    formState.value.name = '';
    formState.value.description = '';
    formState.value.mailAddress = '';
    formState.value.password = '';
    selectedUsers.value = [];
  }
};

const handleBack = () => {
  router.push({name: 'projects'});
};
</script>

<template>
  <div id="bigBox">
    <div id="formHeaderContainer">
      <img id="arrow" src="../../assets/icons/pfeil-links-big.png" @click="handleBack">
      <h3 id="headline">Neues Projekt</h3>
    </div>
    <div id="contentBox">

      <form @submit.prevent="handleSubmit">

        <div class="form-flex-group">
          <div class="form-flex-item">
            <div class="boxLabel">
              <label for="name" class="project-label">Name des Projekts (max. 40 Zeichen)</label><br>
            </div>
            <input
                type="text"
                id="name"
                class="projectForm"
                placeholder="z.Bsp. Maturaball-2025"
                v-model="formState.name"
            >
            <span class="error">{{ errors.name }}</span>
          </div>

          <div class="form-flex-item" style="margin-left: 7%">
            <div class="boxLabel">
              <label for="displayName" class="project-label">Anzeigename</label><br>
            </div>
            <input
                type="text"
                id="displayName"
                class="projectForm"
                placeholder="z.Bsp. Maturaball-Team HTL Leonding"
            >
            <span class="error">{{ errors.name }}</span>
          </div>
        </div>

        <div class="boxLabel">
          <label for="description" class="project-label">Kurzbeschreibung (max. 240 Zeichen)</label><br>
        </div>
        <textarea
            id="description"
            class="projectForm"
            v-model="formState.description"
            maxlength="240"
        ></textarea>
        <span class="error">{{ errors.description }}</span>

        <div class="form-flex-group">
          <div class="form-flex-item">
            <div class="boxLabel">
              <label for="mail" class="project-label">Mail-Adresse</label><br>
            </div>
            <input
                type="email"
                id="mail"
                class="projectForm"
                placeholder="z.Bsp. maturaball-2025@gmail.com"
                v-model="formState.mailAddress"
            >
            <span class="error">{{ errors.email }}</span>
          </div>

          <div class="form-flex-item" style="margin-left: 7%">
            <div class="boxLabel">
              <label for="password" class="project-label">Passwort</label><br>
            </div>
            <input
                type="password"
                id="password"
                class="projectForm"
                v-model="formState.password"
            >
            <span class="error">{{ errors.password }}</span>
          </div>
        </div>

        <div class="boxLabel">
          <label for="recipients" class="project-label">Benutzer hinzufügen</label>
          <div class="input-container">
            <input
                type="text"
                v-model="searchTerm"
                class="projectForm"
                placeholder="Geben Sie hier den Namen eines Benutzers ein..."
                style="width: 35%"
            />
            <ul v-if="searchTerm.length > 0 && filteredUsers.length" class="autocomplete">
              <li v-for="user in filteredUsers" :key="user.id" @click="selectUser(user)">
                {{ user.firstName }} {{ user.lastName }} - {{ user.mailAddress }}
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
          </div>
          <span class="error">{{ errors.selectedUsers }}</span>
        </div>

        <div id="buttonBox">
          <button type="submit" id="saveButton" class="btn btn-primary">Speichern</button>
          <button @click="handleBack" id="cancelButton" class="btn btn-secondary">Abbrechen</button>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
/* Tags für ausgewählte Benutzer/Gruppen */
#selectedUsersList {
  width: 80%;
  height: 15vh;
  border: 1px solid #ccc;
  margin-top: 1vh;
  border-radius: 5px;
  padding: 0.3%;
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
/* header */
#formHeaderContainer {
  display: flex;
  flex-direction: row;
  align-items: center;
  margin-top: 4vh;
}

#arrow {
  width: 1.2vw;
  margin-left: 2%;
}

#headline {
  align-items: center;
  margin-left: 6%;
  font-weight: bold;
}

/* content */
#contentBox {
  margin-top: 4vh;
}

#bigBox {
  width: 100%;
}

/* form */
form {
  display: flex;
  flex-direction: column;
  margin-left: 10%;
  width: 80%;
}

.form-flex-group {
  display: flex;
  flex-direction: row;
  width: 50vw;
}

.form-flex-item{
   width: 25vw;
}

.project-label {
  color: #5A5A5A;
  font-size: 0.9em;
}

.boxLabel{
  margin-top: 1vh;
}

.projectForm::placeholder {
  color: #B3B3B3;
}

.projectForm {
  display: block;
  all: unset;
  border: solid 1px #BEBEBE;
  border-radius: 5px;
  padding: 0.6vw;
  width: 50vw;
  font-size: 0.8em;
  background-color: #F6F6F6;
}

.form-flex-item input{
  width: 100%;
}

.projectForm:focus {
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2);
}

.error {
  color: red;
  font-size: 0.7em;
}

#buttonBox {
  position: absolute;
  top: 80vh;
  right: 15vw;
  width: 12vw;
  margin-top: 0;
}

/* user select */
.multiselect {
  display: block;
  border: solid 1px #BEBEBE;
  border-radius: 5px;
  padding: 0.6vw;
  width: 50%;
  font-size: 0.5em;
  margin-bottom: 3%;
  background-color: #F6F6F6;
  position: relative;
}

.selected {
  display: inline-flex;
  align-items: center;
  background-color: #78A6FF;
  color: white;
  border-radius: 3px;
  padding: 2px 5px;
  margin-right: 5px;
  margin-bottom: 3px;
  font-size: 0.5em;
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
  background-color: #F6F6F6;
  width: 100%;
  padding: 0.6vw;
  box-sizing: border-box;
  border-radius: 5px;
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

.error {
  color: red;
  font-size: 0.7em;
}

/* button */
#submitButton {
  all: unset;
  border-radius: 12px;
  padding: 1vh 0;
  background-color: #78A6FF;
  color: white;
  width: 100%;
  border: #78A6FF solid 1px;
  font-size: 0.8rem;
  text-align: center;
}

#submitButton:hover {
  background-color: rgba(75, 129, 253, 0.86);
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2);
}

#submitButton:disabled {
  background-color: lightgray;
  border-color: lightgray;
}

#submitButton:disabled:hover {
  border-color: lightgray;
  box-shadow: none;
}

</style>
