<script setup lang="ts">
import {computed, type Ref, ref, watch} from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';

const router = useRouter();

interface User {
  id: number;
  firstName: string;
  lastName: string;
  mailAddress: string;
}

const selectedUsers = ref<User[]>([]) as Ref<User[]>;
const searchTerm = ref('');
const users = ref<User[]>([]) as Ref<User[]>;
const loading = ref(false);

const fetchUsers = async (query: string) => {
  loading.value = true;
  try {
    const response = await axios.get(`/api/users/search?query=${query}&kc=true`);
    users.value = response.data;
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
      router.push({name: 'projekte'});
    }).catch((error) => {
      console.error('Error creating project:', error);
    });

    Object.keys(formState.value).forEach(key => {
      key = '';
    });
    selectedUsers.value = [];
  }
};

const handleBack = () => {
  router.push({ name: 'projekte' });
};
</script>

<template>
  <div id="bigBox">
    <img id="pfeil" src="../assets/icons/pfeil-links-big.png" @click="handleBack">
    <div id="contentBox">
      <h3 id="headline">Neues Projekt</h3>

      <form @submit.prevent="handleSubmit">

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

        <div class="boxLabel">
          <label for="description" class="project-label">Kurzbeschreibung (max. 240 Zeichen)</label><br>
        </div>
        <textarea
            id="description"
            class="projectForm"
            v-model="formState.description"
        ></textarea>
        <span class="error">{{ errors.description }}</span>

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

        <div class="boxLabel">
          <label for="users" class="project-label">Benutzer hinzufügen</label><br>
        </div>
        <div class="multiselect">
          <div class="selected" v-for="user in selectedUsers" :key="user.id">
            {{ user.firstName }} {{ user.lastName }} <span class="remove" @click="removeUser(user)">×</span>
          </div>
          <input type="text" v-model="searchTerm" placeholder="Benutzer suchen">
          <ul v-if="searchTerm.length > 0 && filteredUsers.length">
            <li v-for="user in filteredUsers" :key="user.id" @click="selectUser(user)">
              <div class="user-info">
                <span>{{ user.firstName }} {{ user.lastName }}</span>
                <small>{{ user.mailAddress }}</small>
              </div>
            </li>
            <li v-if="loading">Laden...</li>
          </ul>
        </div>
        <span class="error">{{ errors.selectedUsers }}</span>

        <div id="buttonBox">
          <button type="submit" id="submitButton">Projekt erstellen</button>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
#contentBox {
  margin-top: 1%;
}
#pfeil {
  width: 2vw;
  margin-top: 2%;
  margin-left: 2%;
}

#bigBox {
  width: 100%;
}

#headline {
  margin-left: 10%;
  padding-bottom: 3%;
}

form {
  display: flex;
  flex-direction: column;
  margin-left: 15%;
}

.boxLabel {
}

.project-label {
  color: #5A5A5A;
  font-size: 0.8em;
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
  width: 50%;
  font-size: 0.5em;
  margin-bottom: 3%;
  background-color: #F6F6F6;
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
</style>
