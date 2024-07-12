<script setup lang="ts">

import {onMounted, ref, watch} from "vue";
import {Service} from "@/stores/service";
import {Quill, QuillEditor} from "@vueup/vue-quill";

const inputName = ref('');
const inputHeading = ref('');
let selectedGreeting = ref('');

const emit = defineEmits(['template-added']);
const props = defineProps<{ selectedTemplate: { name: string, headline: string, greeting: string } | null }>();

const content = ref('')
const greetingData = ref<{ id: number, content: string }[]>([]);

const getGreetings = async () => {
  const response = await Service.getInstance().getGreetings();
  greetingData.value = response.data;
  console.log('greetings' + greetingData.value)
}

const addTemplate = async () => {
  try {
    const formData = {
      name: inputName.value,
      headline: inputHeading.value,
      content: content.value,
      accountName: 'IT200274',
      greeting: selectedGreeting.value
    };
    console.log(formData)
    const response = await Service.getInstance().addTemplate(formData);
    console.log('Erfolgreich gesendet:', response.data);
    clearForm();
    emit('template-added', formData);  // Emit the event with the new data
  } catch (error) {
    console.error('Fehler beim Senden der Daten:', error);
  }
}

const clearForm = () => {
  inputName.value = '';
  inputHeading.value = '';
  selectedGreeting.value = '';
  content.value = ''
}

const toolbarOptions = ref([
  [{'font': []}],
  [{'size': ['small', false, 'large', 'huge']}],  // custom dropdown
  ['bold', 'italic', 'underline', 'strike'],
  [{'color': []}, {'background': []}],          // dropdown with defaults from theme
  [{'align': []}], // toggled buttons

  [{'list': 'ordered'}, {'list': 'bullet'}, {'list': 'check'}],
  ['blockquote'],

  ['link', 'image', 'clean'],
]);

onMounted(() => {
  console.log('hello')
  getGreetings();
})

/*watch(() => props.selectedTemplate, (newTemplate) => {
  console.log('selected Temp')
  console.log(props.selectedTemplate);
  if (newTemplate) {
    inputName.value = newTemplate.name;
    inputHeading.value = newTemplate.headline;
    selectedGreeting.value = newTemplate.greeting.id;
    console.log(newTemplate)
  }
});*/
</script>

<template>
  <form @submit.prevent="addTemplate">
    <div>
      <label for="name1">Name:</label>
      <input v-model="inputName" type="text" id="name">
    </div>
    <div>
      <label for="betreff1">Betreff:</label>
      <input v-model="inputHeading" type="text" id="betreff">
    </div>
    <div>
      <label for="anrede">Anrede aussuchen:</label>
      <select id="anrede" v-model="selectedGreeting">
        <option disabled value="">Please select one</option>
        <option v-for="item in greetingData" :key="item.content" :value="item.id">{{ item.content }}</option>
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