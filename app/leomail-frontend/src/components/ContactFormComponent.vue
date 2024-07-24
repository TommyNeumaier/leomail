<script setup lang="ts">
import {onMounted, ref} from "vue";
import {Service} from "@/stores/service";

const picked = ref<string>(''); // Holds the value of the selected gender
const pickedEntity = ref<string>(''); // Holds the value of the selected entity
const checkedUnternehmen = ref(false);
const checkedPrivatperson = ref(false);
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
  if (checkedPrivatperson){
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
        <label for="checkboxUnternehmen">juristische Person</label>
        <input
            type="checkbox"
            class="checkbox"
            id="checkboxUnternehmen"
            value="unternehmen"
            v-model="checkedUnternehmen"
            @change="handleEntities('unternehmen')"
        />

        <label for="checkboxPrivatperson">natürliche Person</label>
        <input
            type="checkbox"
            class="checkbox"
            id="checkboxPrivatperson"
            value="privatperson"
            v-model="checkedPrivatperson"
            @change="handleEntities('privatperson')"
        />

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

          <div>
            <label for="titel" class="personen-label">Titel (optional)</label><br>
            <input type="text" id="titel" class="formPerson">
          </div>
          <div id="nameBox">
            <div>
              <label for="firstName" class="personen-label">Vorname</label><br>
              <input type="text" id="firstName" class="formPerson" v-model="firstname" required>
            </div>
            <div>
              <label for="lastName" class="personen-label">Nachname</label><br>
              <input type="text" id="lastName" class="formPerson" v-model="lastname" required>
            </div>
          </div>
          <div>
            <label for="email" class="personen-label">Email</label><br>
            <input type="email" id="email" class="formPerson" placeholder="z.Bsp. max.muster@gmail.com" v-model="email" required>
          </div>
          <div>
            <div>
              <label for="company" class="personen-label">Firma (optional)</label><br>
              <input type="text" id="company" class="formPerson">
            </div>
            <div>
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
          <button type="submit">Person erstellen</button>
        </div>
      </div>
    </div>
  </form>
</template>

<style scoped>
button {
  all: unset;
  background-color: #78A6FF;
  border-radius: 10px;
  padding: 1% 3%;
  font-size: 0.5rem;
  color: white;
}

button:hover {
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2);
}

#buttonBox {
  display: flex;
  justify-content: right;
}

#nameBox {
  display: flex;
  flex-direction: row;
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
  align-items: center;
}

#checkBox div label {
  margin-right: 10%;
}

#formular {
  margin-top: 5%;
}

#headline {
  font-weight: bold;
}

#contentContainer {
  padding: 3% 5%;
}
</style>
