<script setup lang="ts">

import {onMounted, ref} from "vue";
import {Service} from "@/stores/service";
import {Quill, QuillEditor} from "@vueup/vue-quill";

const inputName = ref('');
const inputBetreff = ref('');
const selectAnrede = ref('');

const emit = defineEmits(['vorlage-added']);

const content = ref("dj")
const anredeData = ref<{ id: number, content: string }[]>([]);

const getGreetings = async () => {
  const response = await Service.getInstance().getGreetings();
  anredeData.value = response.data;
}

const addVorlage = async () => {
  try {
    const formData = {
      name: inputName.value,
      headline: inputBetreff.value,
      content: content.value,
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

const toolbarOptions = ref([
  [{ 'font': [] }],
  [{ 'size': ['small', false, 'large', 'huge'] }],  // custom dropdown
  ['bold', 'italic', 'underline', 'strike'],
  [{ 'color': [] }, { 'background': [] }],          // dropdown with defaults from theme
  [{ 'align': [] }], // toggled buttons

  [{ 'list': 'ordered'}, { 'list': 'bullet' }, { 'list': 'check' }],
  ['blockquote'],

  ['link', 'image', 'clean'],
]);

onMounted (() => {
  getGreetings();
})
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
        <option v-for="item in anredeData" :key="item.content" :value="item.id">{{ item.content }}</option>
      </select>
    </div>
    <QuillEditor :toolbar="toolbarOptions" v-model:content="content" content-type="html"/>
    <div>
    </div>
    <button type="submit">Absenden</button>
  </form>

</template>

<style scoped>

</style>