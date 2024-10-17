<script setup lang="ts">
import {ref, onMounted, watch, computed} from 'vue';
import {Service} from "@/services/service";
import {useAppStore} from "@/stores/app.store";

interface User {
  id: number;
  firstName: string;
  lastName: string;
  mailAddress: string;
}

interface Group {
  id: string;
  name: string;
  description: string;
  members: User[];
}

const appStore = useAppStore();
const emitEvents = defineEmits(['group-added', 'group-removed', 'group-saved']);
const props = defineProps<{ selectedTemplate: Group | null }>();

const groupName = ref('');
const groupDescription = ref('');
const selectedMembers = ref<User[]>([]);
const users = ref<User[]>([]);
const searchTerm = ref('');
const loading = ref(false);

const clearForm = () => {
  groupName.value = '';
  groupDescription.value = '';
  selectedMembers.value = [];
};

// Stellen Sie sicher, dass die Methode verfügbar ist
defineExpose({ clearForm });

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
    const response = await Service.getInstance().addGroup(appStore.$state.project, formData);
    console.log('Group created:', response.data);
    emitEvents('group-added', formData);
    clearForm();
  } catch (error) {
    console.error('Error adding group:', error);
  }
};

const updateGroup = async () => {
  try {
    if (!props.selectedTemplate) return;
    const formData = {
      id: props.selectedTemplate.id,
      name: groupName.value,
      description: groupDescription.value,
      members: selectedMembers.value
    };
    const response = await Service.getInstance().updateGroup(appStore.$state.project, formData);
    console.log('Group updated:', response.data);
    emitEvents('group-saved', formData);
    clearForm();
  } catch (error) {
    console.error('Error updating group:', error);
  }
};

const removeGroup = async () => {
  try {
    if (!props.selectedTemplate) return;
    await Service.getInstance().deleteGroup(appStore.$state.project, props.selectedTemplate.id);
    emitEvents('group-removed', props.selectedTemplate);
    clearForm();
  } catch (error) {
    console.error('Error removing group:', error);
  }
};

watch(() => props.selectedTemplate, (newTemplate) => {
  if (newTemplate) {
    groupName.value = newTemplate.name;
    groupDescription.value = newTemplate.description;
    selectedMembers.value = newTemplate.members;
  } else {
    clearForm();
  }
});

onMounted(() => {
  if (props.selectedTemplate) {
    groupName.value = props.selectedTemplate.name;
    groupDescription.value = props.selectedTemplate.description;
    selectedMembers.value = props.selectedTemplate.members;
  }
});

</script>

<template>
  <div id="bigBox">
    <form @submit.prevent="props.selectedTemplate ? updateGroup() : addGroup()">
      <div class="dataBox">
        <div class="boxLabel">
          <label for="groupName" class="group-label">Gruppenname</label><br>
        </div>
        <input
            type="text"
            id="groupName"
            class="formGroup"
            placeholder="Geben Sie einen Gruppennamen ein..."
            v-model="groupName"
        >
      </div>

      <div class="dataBox">
        <div class="boxLabel">
          <label for="groupDescription" class="group-label">Beschreibung</label><br>
        </div>
        <textarea
            id="groupDescription"
            class="formGroup"
            placeholder="Geben Sie eine kurze Beschreibung zur Gruppe ein..."
            v-model="groupDescription"
        ></textarea>
      </div>

      <div class="dataBox">
        <div class="boxLabel">
          <label for="members" class="group-label">Mitglieder hinzufügen</label><br>
        </div>
        <div class="multiselect">
          <div class="selected" v-for="user in selectedMembers" :key="user.id">
            {{ user.firstName }} {{ user.lastName }} <span class="remove" @click="removeUser(user)">×</span>
          </div>
          <input type="text" v-model="searchTerm" class="formGroup" placeholder="Benutzer suchen">
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
      </div>

      <div id="buttonBox">
        <button type="submit" id="submitButton">{{ props.selectedTemplate ? 'Speichern' : 'Erstellen' }}</button>
        <button type="button" @click="removeGroup" v-if="props.selectedTemplate" id="deleteButton">Löschen</button>
        <!--<button type="button" @click="clearForm">Abbrechen</button>-->
      </div>
    </form>
  </div>
</template>

<style scoped>
.dataBox {
  margin-bottom: 1%;
}

form {
  padding: 2% 3%;
}

#bigBox {
  width: 100%;
}

.group-label {
  color: #5A5A5A;
  font-size: 1em;
}

.formGroup::placeholder {
  color: #B3B3B3;
}

.formGroup {
  display: block;
  border: solid 1px #BEBEBE;
  border-radius: 5px;
  padding: 0.6vw;
  width: 60%;
  font-size: 0.8em;
}

.formGroup:focus {
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2);
}

.multiselect {
  display: block;
  border: solid 1px #BEBEBE;
  border-radius: 5px;
  padding: 0.6vw;
  width: 60%;
  font-size: 0.8em;
  margin-bottom: 3%;
  position: relative;
}

.selected {
  display: inline-flex;
  align-items: center;
  background-color: lightblue;
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
}

#submitButton {
  all: unset;
  border-radius: 12px;
  padding: 0.5vh 0;
  background-color: #78A6FF;
  color: white;
  width: 10%;
  border: #78A6FF solid 1px;
  font-size: 0.8rem;
  text-align: center;
}

#deleteButton{
  all: unset;
  border-radius: 12px;
  padding: 0.5vh 0;
  color: red;
  width: 10%;
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

#deleteButton:hover {
 font-weight: bold;
}
</style>