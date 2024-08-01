<script setup lang="ts">
import {QuillEditor} from "@vueup/vue-quill";
import {Service} from "@/services/service";
import {ref} from "vue";


const addGroup = async () => {
  /*try {
    const formData = {
      name: inputName.value,
      headline: inputHeading.value,
      content: content.value,
      accountName: 'IT200274',
      greeting: selectedGreeting.value
    };
    console.log(formData);
    const response = await Service.getInstance().addTemplate(formData);
    console.log('Erfolgreich gesendet:', response.data);
    clearForm();
    emit('template-added', formData);
  } catch (error) {
    console.error('Fehler beim Senden der Daten:', error);
  }*/
};

const updateGroup = async () => {
  try {
    //console.log(updatedData);
    //const response = await Service.getInstance().addTemplate(updatedData);
    //console.log('Erfolgreich gesendet:', response.data);
    //clearForm();
    //emit('template-added', updatedData);
  } catch (error) {
    console.error('Fehler beim Speichern der Daten:', error);
  }
};

const removeGroup = async () => {
  try {
    //console.log(props.selectedTemplate);
    //const response = await Service.getInstance().removeTemplate(props.selectedTemplate);
    //console.log('Erfolgreich gesendet:', response.data);
    //clearForm();
    //emit('template-added', updatedData);
  } catch (error) {
    console.error('Fehler beim Löschen der Daten:', error);
  }
};

const inputName = ref('');
const inputGroupDescription = ref('');
const selectedPeople = ref('');
const selectedTemplate = ref('');

</script>

<template>
  <form @submit.prevent="addGroup">
    <div>
      <label for="name1" class="group-label">Gruppenname</label><br>
      <input v-model="inputName" type="text" id="name" class="formGroup"
             placeholder="Geben Sie einen Gruppennamen ein ...">
    </div>
    <div>
      <label for="groupDescription" class="group-label">Gruppenbeschreibung (optional)</label><br>
      <input v-model="inputGroupDescription" type="text" class="formGroup"
             placeholder="Geben Sie eine kurze Beschreibung zur Gruppe ..." id="groupDescription">
    </div>

    <div id="mitgliederBox">
      <label for="mitglieder" class="group-label">Gruppenmitglieder</label><br>
      <select id="mitglieder" class="formGroup" v-model="selectedPeople">
        <option disabled value="" id="placeholderSelectionGroup">Geben Sie einen Namen oder eine E-Mail-Adresse ein ...</option>
        <!--<option v-for="item in greetingData" :key="item.id" :value="item.id">{{ item.content }}</option>-->
      </select>
    </div>

    <div id="buttonBox">
      <button type="submit" class="saveTemplate" :disabled="selectedTemplate != null">Erstellen</button>
      <button type="button" @click=updateGroup class="saveTemplate" :disabled="selectedTemplate == null">Löschen</button>
      <button type="button" @click=removeGroup class="saveTemplate" :disabled="selectedTemplate == null">Speichern</button>
    </div>
  </form>

</template>

<style scoped>
form {
  padding: 2% 3%;
}

form div{
  margin-top: 1%;
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
  width: 40%;
  font-size: 0.5em;
}

.formGroup:focus {
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2);
}

#placeholderSelectionGroup {
  color: #5A5A5A;
  font-size: 0.5em;
}
</style>