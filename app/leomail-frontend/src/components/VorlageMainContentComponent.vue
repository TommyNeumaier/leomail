<script setup lang="ts">

import {ref} from "vue";
import {Service} from "@/stores/service";

const inputName = ref('');
const inputBetreff = ref('');
const selectAnrede = ref('');
const content = ref('');

const emit = defineEmits(['vorlage-added']);

const addVorlage = async () => {
  try {
    const formData = {
      name: inputName.value,
      headline: inputBetreff.value,
      content: selectAnrede.value,
      accountName: 'IT200274'
    };
    console.log(formData)
    const response = await Service.getInstance().addVorlage(formData);
    console.log('Erfolgreich gesendet:', response.data);
    clearForm();
    emit('vorlage-added', formData);  // Emit the event with the new data
  } catch (error) {
    console.error('Fehler beim Senden der Daten:', error);
  }
}

const clearForm = () => {
  inputName.value = '';
  inputBetreff.value = '';
  selectAnrede.value = '';
}
</script>

<template>
  <form @submit.prevent="addVorlage">
    <div>
      <label for="name1">Name:</label>
      <input v-model="inputName" type="text" id="name">
    </div>
    <div>
      <label for="betreff1">Betreff:</label>
      <input v-model="inputBetreff" type="text" id="betreff">
    </div>
    <div>
      <label for="anrede" >Anrede aussuchen:</label>
      <select id="anrede" v-model="selectAnrede">
        <option disabled value="">Please select one</option>
        <option value="Hallo">Hallo</option>
        <option value="Sehr geehrte">Sehr geehrte</option>
      </select>
    </div>
    <div>
    </div>
    <button type="submit">Absenden</button>
  </form>

</template>

<style scoped>

</style>