<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue';
import { Service } from "@/services/service";
import { useRoute } from 'vue-router';
import { useAppStore } from "@/stores/app.store";

const route = useRoute();
const appStore = useAppStore();

interface User {
  id: number;
  firstName: string;
  lastName: string;
  mailAddress: string;
}

const groupName = ref('');
const groupDescription = ref('');
const selectedMembers = ref<User[]>([]);
const selectedGroup = ref<{ id: string, name: string, description: string, members: User[] } | null>(null);
const groups = ref<{ id: string, name: string, description: string, members: User[] }[]>([]);
const searchQuery = ref('');
const users = ref<User[]>([]);
const searchTerm = ref('');
const loading = ref(false);

const getGroups = async () => {
  try {
    const response = await Service.getInstance().getPersonalGroups(appStore.$state.project);
    groups.value = response.data;
  } catch (error) {
    console.error('Fehler beim Laden der Gruppen:', error);
  }
};

const fetchUsers = async (query: string) => {
  loading.value = true;
  try {
    const response = await Service.getInstance().searchContacts(query);
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
  if (!selectedMembers.value.find(u => u.id === user.id)) {
    selectedMembers.value.push(user);
  }
  searchTerm.value = '';
  users.value = [];
};

const removeUser = (user: User) => {
  selectedMembers.value = selectedMembers.value.filter(u => u.id !== user.id);
};

const addGroup = async () => {
  try {
    const formData = {
      name: groupName.value,
      description: groupDescription.value,
      members: selectedMembers.value
    };
    await Service.getInstance().addGroup(appStore.$state.project, formData);
    await getGroups();
    clearForm();
  } catch (error) {
    console.error('Fehler beim Senden der Daten:', error);
  }
};

const updateGroup = async () => {
  try {
    if (!selectedGroup.value) return;
    const formData = {
      id: selectedGroup.value.id,
      name: groupName.value,
      description: groupDescription.value,
      members: selectedMembers.value.map(user => user.id)
    };
    await Service.getInstance().updateGroup(appStore.$state.project, formData);
    await getGroups();
    clearForm();
  } catch (error) {
    console.error('Fehler beim Aktualisieren der Gruppe:', error);
  }
};

const removeGroup = async () => {
  try {
    if (!selectedGroup.value) return;
    await Service.getInstance().deleteGroup(appStore.$state.project, selectedGroup.value.id);
    await getGroups();
    clearForm();
  } catch (error) {
    console.error('Fehler beim Löschen der Gruppe:', error);
  }
};

const clearForm = () => {
  groupName.value = '';
  groupDescription.value = '';
  selectedMembers.value = [];
  selectedGroup.value = null;
};

onMounted(() => {
  getGroups();
});

const filteredGroups = computed(() => {
  if (!searchQuery.value) return groups.value;
  return groups.value.filter(group => group.name.toLowerCase().includes(searchQuery.value.toLowerCase()));
});

const selectGroup = (group: { id: string, name: string, description: string, members: User[] }) => {
  selectedGroup.value = group;
  groupName.value = group.name;
  groupDescription.value = group.description;
  selectedMembers.value = group.members;
};

</script>

<template>
  <div id="bigBox">
    <h3>Gruppenverwaltung</h3>
    <form @submit.prevent="selectedGroup ? updateGroup() : addGroup()">
      <div class="boxLabel">
        <label for="groupName" class="group-label">Gruppenname:</label><br>
      </div>
      <input
          type="text"
          id="groupName"
          class="formGroup"
          placeholder="Geben Sie einen Gruppennamen ein..."
          v-model="groupName"
      >
      <div class="boxLabel">
        <label for="groupDescription" class="group-label">Beschreibung:</label><br>
      </div>
      <textarea
          id="groupDescription"
          class="formGroup"
          placeholder="Geben Sie eine kurze Beschreibung zur Gruppe ein..."
          v-model="groupDescription"
      ></textarea>
      <div class="boxLabel">
        <label for="members" class="group-label">Mitglieder hinzufügen:</label><br>
      </div>
      <div class="multiselect">
        <div class="selected" v-for="user in selectedMembers" :key="user.id">
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

      <div id="buttonBox">
        <button type="submit" id="submitButton">{{ selectedGroup ? 'Aktualisieren' : 'Erstellen' }}</button>
        <button type="button" @click="removeGroup" v-if="selectedGroup">Löschen</button>
        <button type="button" @click="clearForm">Abbrechen</button>
      </div>
    </form>

    <div>
      <input type="text" v-model="searchQuery" placeholder="Gruppen suchen" class="formGroup"/>
      <ul>
        <li v-for="group in filteredGroups" :key="group.id" @click="selectGroup(group)">
          {{ group.name }}
        </li>
      </ul>
    </div>
  </div>
</template>

<style scoped>
#bigBox {
  width: 100%;
}

.group-label {
  color: #5A5A5A;
  font-size: 0.8em;
}

.formGroup::placeholder {
  color: #B3B3B3;
}

.formGroup {
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

.formGroup:focus {
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2);
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

#buttonBox {
  display: flex;
  gap: 10px;
}

#submitButton {
  all: unset;
  border-radius: 12px;
  padding: 1vh 0;
  background-color: #78A6FF;
  color: white;
  width: 15%;
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