<script setup lang="ts">
import {computed, onMounted, ref, watch} from "vue";
import {Service} from "@/stores/service";

interface Contact {
  id: string | null,
  firstName: string,
  lastName: string,
  mailAddress: string,
  gender?: string,
  positionAtCompany?: string;
  company?: string;
  prefixTitle?: string;
  suffixTitle?: string;
}

const picked = ref<string | null>(); // Holds the value of the selected gender
const pickedEntity = ref<string>(''); // Holds the value of the selected entity
const checkedUnternehmen = ref(false);
const checkedPrivatperson = ref(true);
const firstname = ref('');
const lastname = ref('');
const email = ref('');
const positionAtCompany = ref<string | null>();
const company = ref<string | null>();
const prefixTitle = ref<string | null>();
const suffixTitle = ref<string | null>();
const propsSelected = defineProps<{ selectedContact: Contact | null }>();
const emit = defineEmits(['contact-deleted', 'contact-updated', 'contact-added']);

watch(
    () => propsSelected.selectedContact,
    (newContact) => {
      clearForm();
      console.log(newContact)
      if (newContact) {
        console.log(newContact);
        firstname.value = newContact.firstName;
        lastname.value = newContact.lastName;
        email.value = newContact.mailAddress;
        if (newContact.gender) picked.value = newContact.gender;
        if (newContact.positionAtCompany) positionAtCompany.value = newContact.positionAtCompany;
        if (newContact.company) company.value = newContact.company;
        if (newContact.prefixTitle) prefixTitle.value = newContact.prefixTitle;
        if (newContact.suffixTitle) suffixTitle.value = newContact.suffixTitle;
      }
    }
);


const clearForm = () => {
  firstname.value = '';
  lastname.value = '';
  email.value = '';
  picked.value = null;
  positionAtCompany.value = null;
  company.value = null;
  prefixTitle.value = null;
  suffixTitle.value = null;
}
const handleEntities = (entity: string) => {
  pickedEntity.value = entity;
  if (entity === 'unternehmen') {
    checkedUnternehmen.value = true;
    checkedPrivatperson.value = false;
  } else if (entity === 'privatperson') {
    checkedUnternehmen.value = false;
    checkedPrivatperson.value = true;
  }
};

/*const saveContact = async () => {
  if (checkedPrivatperson && picked.value != null) {
    try {
      const contactForm = {
        company: company.value,
        positionAtCompany: positionAtCompany.value,
        prefixTitle: prefixTitle.value,
        suffixTitle: suffixTitle.value,
        gender: picked.value,
        firstName: firstname.value,
        lastName: lastname.value,
        mailAddress: email.value
    };
      console.log(contactForm);
      const response = await Service.getInstance().addContact(contactForm);
      console.log('Erfolgreich gesendet:', response.data);
      clearForm();
    } catch (error) {
      console.error('Fehler beim Senden der Daten:', error);
    }
  }
}*/

const saveOrUpdateContact = async () => {
  try {
    const contactForm = {
      id: propsSelected.selectedContact?.id,
      company: company.value,
      positionAtCompany: positionAtCompany.value,
      prefixTitle: prefixTitle.value,
      suffixTitle: suffixTitle.value,
      gender: picked.value,
      firstName: firstname.value,
      lastName: lastname.value,
      mailAddress: email.value
    };
    if (propsSelected.selectedContact) {
      await Service.getInstance().updateContact(propsSelected.selectedContact.id, contactForm);
      console.log('Kontakt erfolgreich aktualisiert');
      emit('contact-updated');
    } else {
      await Service.getInstance().addContact(contactForm);
      console.log('Kontakt erfolgreich erstellt');
      emit('contact-added');
    }
    clearForm();
  } catch (error) {
    console.error('Fehler beim Speichern der Daten:', error);
  }
}


const deleteContact = async () => {
  if (propsSelected.selectedContact) {
    const confirmed = confirm('Möchten Sie diesen Kontakt wirklich löschen?');
    if (confirmed) {
      try {
        console.log(propsSelected.selectedContact)
        await Service.getInstance().deleteContact(propsSelected.selectedContact.id);
        console.log('Kontakt erfolgreich gelöscht');
        clearForm();
        emit('contact-deleted'); // Notify parent component
      } catch (error) {
        console.error('Fehler beim Löschen des Kontakts:', error);
      }
    }
  }
}

  const contactName = computed(() => {
    return propsSelected.selectedContact ? `${propsSelected.selectedContact.firstName} ${propsSelected.selectedContact.lastName}` : 'Neue Person';
  });
</script>

<template>
  <form @submit.prevent="saveOrUpdateContact">
    <div id="contentContainer">
      <h3 id="headline"> {{ contactName }}</h3>
      <div id="formular">

        <div class="personContainer">
          <div class="personBox">
            <label for="checkboxPrivatperson">natürliche Person</label>
            <input
                type="checkbox"
                class="checkbox"
                id="checkboxPrivatperson"
                value="privatperson"
                v-model="checkedPrivatperson"
                @change="handleEntities('privatperson')"
            /></div>

          <div class="personBox">
            <label for="checkboxUnternehmen">juristische Person</label>
            <input
                type="checkbox"
                class="checkbox"
                id="checkboxUnternehmen"
                value="unternehmen"
                v-model="checkedUnternehmen"
                @change="handleEntities('unternehmen')"
            />
          </div>
        </div>

        <div v-if="checkedPrivatperson">
          <div id="checkBoxGenderContainer">
            <label class="personen-label">Geschlecht</label><br>
            <div id="checkBox">
              <div id="boxMale">
                <label for="checkboxMale">männlich</label>
                <input
                    type="radio"
                    class="radio"
                    id="checkboxMale"
                    value="M"
                    v-model="picked"
                    required
                />
              </div>
              <div id="boxFemale">
                <label for="checkboxFemale">weiblich</label>
                <input
                    type="radio"
                    class="radio"
                    id="checkboxFemale"
                    value="W"
                    v-model="picked"
                    required
                />
              </div>
            </div>
          </div>

          <div id="titleBox" class="inputBox">
            <div>
              <label for="titelPrefix" class="personen-label">Titel prefix</label><br>
              <input type="text" id="titelPrefix" class="formPerson" placeholder="z.Bsp. Dr." v-model="prefixTitle">
            </div>
            <div>
              <label for="titelSuffix" class="personen-label">Titel suffix</label><br>
              <input type="text" id="titelSuffix" class="formPerson" placeholder="z.Bsp. PhD" v-model="suffixTitle">
            </div>
          </div>

          <div id="nameBox" class="inputBox">
            <div>
              <label for="firstName" class="personen-label">Vorname</label><br>
              <input type="text" id="firstName" class="formPerson" v-model="firstname" required>
            </div>
            <div>
              <label for="lastName" class="personen-label">Nachname</label><br>
              <input type="text" id="lastName" class="formPerson" v-model="lastname" required>
            </div>
          </div>

          <div class="inputBox">
            <label for="email" class="personen-label">Email</label><br>
            <input type="email" id="email" class="formPerson" placeholder="z.Bsp. max.muster@gmail.com" v-model="email"
                   required>
          </div>
          <div>
            <div class="inputBox">
              <label for="company" class="personen-label">Firma (optional)</label><br>
              <input type="text" id="company" class="formPerson" v-model="company">
            </div>
            <div class="inputBox">
              <label for="position" class="personen-label">Position (optional)</label><br>
              <input type="text" id="position" class="formPerson" v-model="positionAtCompany">
            </div>
          </div>
        </div>

        <div v-if="checkedUnternehmen">
          <div>
            <label for="companyName" class="personen-label">Unternehmensbezeichnung</label><br>
            <input type="text" id="companyName" class="formPerson" required>
          </div>
          <div>
            <label for="emailCompany" class="personen-label">E-Mail</label><br>
            <input type="email" id="emailCompany" class="formPerson" required>
          </div>
        </div>
        <div id="buttonBox">
          <button type="submit" id="submitButton">
            {{ propsSelected.selectedContact ? 'Person aktualisieren' : 'Person erstellen' }}
          </button>
          <button type="button" id="deleteButton" @click="deleteContact" v-if="propsSelected.selectedContact">Person
            löschen
          </button>
        </div>
      </div>
    </div>
  </form>
</template>

<style scoped>
.inputBox {
  margin-top: 1vh;
}

#titleBox {
  display: flex;
  flex-direction: row;
  width: 100%;
}

#titleBox div {
  width: 15%;
}

#titelPrefix, #titelSuffix {
  width: 60%;
}

#email, #company, #position {
  width: 28%;
}

.personBox {
  display: flex;
  flex-direction: row;
  width: 20%;
}

.personBox label {
  margin-right: 5%;
}

.personContainer {
  display: flex;
  flex-direction: row;
  width: 100%;
}

#nameBox {
  display: flex;
  flex-direction: row;
  width: 100%;
}

#nameBox div {
  width: 30%;
}

#firstName, #lastName {
  width: 80%;
}

.personen-label {
  color: #5A5A5A;
  font-size: 0.8em;
}

.formPerson::placeholder {
  color: #B3B3B3;
}

.formPerson {
  display: block;
  all: unset;
  border: solid 1px #D3D3D3;
  border-radius: 5px;
  padding: 0.6vw;
  font-size: 0.5em;
  background-color: #F6F6F6;
}

.formPerson:focus {
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2);
}

#checkBox {
  display: flex;
  flex-direction: row;
}

#checkBox div {
  width: 15%;
}

#checkBox div label {
  margin-right: 5%;
}

#checkBoxGenderContainer {
  margin-top: 2%;
}

#formular {
  margin-top: 5%;
  height: 60vh;
}

#headline {
  font-weight: bold;
}

#contentContainer {
  padding: 3% 5%;
}

#buttonBox {
  position: absolute; /* Added */
  top: 80vh; /* Adjust as needed */
  right: 15vw; /* Adjust as needed */
  width: 12vw;
  margin-top: 0; /* Removed margin-top */
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
  border-color: lightgray;;
}

#submitButton:disabled:hover {
  border-color: lightgray;
  box-shadow: none;
}
</style>
