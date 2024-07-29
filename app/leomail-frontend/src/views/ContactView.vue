<script setup lang="ts">

import HeaderComponent from "@/components/HeaderComponent.vue";
import PersonenFormComponent from "@/components/ContactFormComponent.vue";
import {Service} from "@/services/service";
import {computed, onMounted, ref, watch} from "vue";
import Paginator from 'primevue/paginator';

interface Contact {
  id: string,
  firstName: string,
  lastName: string,
  mailAddress: string,
  gender? : string,
  positionAtCompany?: string,
  company?: string,
  prefixTitle?: string,
  suffixTitle?: string
}

const kkc = ref(null);
const selectedContact = ref<Contact | null>(null);
let contactData = ref<Contact[]>([]);
const searchQuery = ref();
const filteredContacts = ref<Contact[]>([]);
const selectedContactIndex = ref();


const getContacts = async () => {
  const response = await Service.getInstance().getContacts();
  contactData.value = response.data;
  filteredContacts.value = response.data;
  console.log(filteredContacts.value);
};

const handleClickedContact = async (item:Contact, index : number) => {
  selectedContactIndex.value = index;

  const response = await Service.getInstance().getContact(item.id);
  selectedContact.value = response.data;
  console.log(selectedContact.value);
}

const searchContacts = async (query: string) => {
  if (query === '') {
    filteredContacts.value = contactData.value;
  } else {
    const response = await Service.getInstance().searchContacts(query);
    console.log(response.data)
    filteredContacts.value = response.data;
  }
};

const handleDeleteContact = async () => {
  if (selectedContact.value) {
    await Service.getInstance().deleteContact(selectedContact.value.id);
    getContacts();
    selectedContact.value = null;
  }
}

watch(searchQuery, () => {
  searchContacts(searchQuery.value);
});

const handleNewContact = () => {
  selectedContact.value = null;
  selectedContactIndex.value = null;
}

const handleContactDeleted = () => {
  getContacts();
  selectedContact.value = null;
  selectedContactIndex.value = null;
}

const handleContactUpdated = async () => {
  await getContacts();
  selectedContact.value = null;
  selectedContactIndex.value = null;
};

const handleAddedContact = () => {
  getContacts();
  selectedContact.value = null;
  selectedContactIndex.value = null;
};

onMounted( () => {
  getContacts();
})
</script>

<template>
  <header-component></header-component>
  <div id="bigContainer">
    <div id="listContainer">

      <div id="flexHeadline">
        <div>
          <h3 id="headline">Personen</h3></div>
        <div><div @click="handleNewContact" id="newContact"><img src="../assets/icons/newMail-white.png"></div></div>
      </div>

      <div id="search-container">
        <div id="searchIconBox">
          <img src="../assets/icons/search.png" alt="Suche" id="search-icon" width="auto" height="10">
        </div>
        <input type="text" id="search" placeholder="suche" v-model="searchQuery">
      </div>

      <div id="contactsBoxContainer">
        <a v-for="(item, index) in filteredContacts" :key="index" @click="handleClickedContact(item,index)" class="contactItems" :id="String('contact-' + index)"
           :class="{ highlighted: selectedContactIndex === index, 'font-bold': selectedContactIndex === index }">
          {{ item.firstName }} {{ item.lastName }}<br>
        </a>
      </div>

    </div>

    <div id="contentContainer">
      <personen-form-component :selectedContact="selectedContact" @contact-deleted="handleContactDeleted" @contact-updated="handleContactUpdated" @contact-added="handleAddedContact"
      ></personen-form-component>
    </div>
  </div>
</template>

<style scoped>
#flexHeadline{
  display: flex;
  flex-direction: row;
}

#flexHeadline *{
  width: 50%;
}
#newContact {
  background-color: #0086D4;
  border-radius: 25%;
  border-color: #0086D4;
  display: flex;
  justify-content: center;
  align-items: center;
  width: 18%;
  margin-left: 70%;
  margin-top: 2vh;
}

#newContact:focus{
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2);
}

#newContact:hover{
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2);
}

#newContact img {
  width: 100%;
  height: auto;
  padding: 10%;
}

.contactItems.highlighted{
  font-weight: bold;
}
#contactsBoxContainer a:hover{
  cursor: pointer;
}
#contactsBoxContainer{
  margin-left: 10%;
  margin-top: 5%;
}
#headline{
  font-weight: var(--font-weight-medium);
  padding: 2vh 0 1vh 1.5vw;
}

#searchIconBox {
  display: flex;
  align-items: center;
  justify-content: center;
}

#search-container {
  display: flex;
  flex-direction: row;
  width: 90%;
  margin: auto;
  border-radius: 5px;
  padding: 2% 4%;
  background-color: #ECECEC;
}

#search {
  all: unset;
  width: 90%;
  margin-left: 3%;
  font-size: 0.3rem;
}

#bigContainer{
  width: 80%;
  height: 80vh;
  display: flex;
  flex-direction: row;
  margin: auto;
  margin-top: 4vh;
  box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.12);
}
#listContainer{
  width: 25%;
  border-right: rgba(0, 0, 0, 0.20) solid 2px;
}
#contentContainer{
  width: 75%;
}
</style>