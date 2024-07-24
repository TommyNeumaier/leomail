<script setup lang="ts">
import {onMounted, ref} from "vue";
import {Service} from "@/stores/service";

const picked = ref<string>(''); // Holds the value of the selected gender
const pickedEntity = ref<string>(''); // Holds the value of the selected entity
const checkedUnternehmen = ref(false);
const checkedPrivatperson = ref(true);
const firstname = ref('');
const lastname = ref('');
const email = ref('');

const handleGenderChange = (gender: string) => {
  picked.value = gender;
};

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

const saveContact = async () => {
  if (checkedPrivatperson) {
    try {
      const contactForm = {
        firstName: firstname.value,
        lastName: lastname.value,
        mailAddress: email.value
        // TODO: add prefixTitle, suffixTitle, company, positionAtCompany, gender (M/W)
    };
      console.log(contactForm);
      const response = await Service.getInstance().addContact(contactForm);
      console.log('Erfolgreich gesendet:', response.data);
    } catch (error) {
      console.error('Fehler beim Senden der Daten:', error);
    }
  }
}
</script>

<template>
  <form @submit.prevent="saveContact">
    <div id="contentContainer">
      <h3 id="headline">Neue Person</h3>
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
                    value="male"
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
                    value="female"
                    v-model="picked"
                    required
                />
              </div>
            </div>
          </div>

          <div id="titleBox" class="inputBox">
            <div>
              <label for="titelPrefix" class="personen-label">Titel prefix</label><br>
              <input type="text" id="titelPrefix" class="formPerson" placeholder="z.Bsp. Dr.">
            </div>
            <div>
              <label for="titelSuffix" class="personen-label">Titel suffix</label><br>
              <input type="text" id="titelSuffix" class="formPerson" placeholder="z.Bsp. PhD">
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
              <input type="text" id="company" class="formPerson">
            </div>
            <div class="inputBox">
              <label for="position" class="personen-label">Position (optional)</label><br>
              <input type="text" id="position" class="formPerson">
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
          <button type="submit" id="submitButton">Person erstellen</button>
        </div>
      </div>
    </div>
  </form>
</template>

<style scoped>
.inputBox{
  margin-top: 1vh;
}
#titleBox{
  display: flex;
  flex-direction: row;
  width: 100%;
}
#titleBox div{
  width: 15%;
}

#titelPrefix, #titelSuffix{
  width: 60%;
}

#email,#company,#position{
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
