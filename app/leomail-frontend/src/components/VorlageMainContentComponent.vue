<script setup lang="ts">

import {onMounted, ref, watch} from "vue";
import {Service} from "@/stores/service";
import {Quill, QuillEditor} from "@vueup/vue-quill";

const inputName = ref('');
const inputHeading = ref('');
let selectedGreeting = ref('');

const emit = defineEmits(['template-added']);
const props = defineProps<{
  selectedTemplate: { name: string, headline: string, greeting: string, content: string } | null
}>();

let content = ref('')
const greetingData = ref<{ id: number, content: string }[]>([]);


const getGreetings = async () => {
  const response = await Service.getInstance().getGreetings();
  greetingData.value = response.data;
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
  content.value = '';
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
  getGreetings();
})

watch(() => props.selectedTemplate, (newTemplate) => {
  console.log('selected Temp')
  console.log(props.selectedTemplate);
  if (newTemplate) {
    inputName.value = newTemplate.name;
    inputHeading.value = newTemplate.headline;
    selectedGreeting.value = newTemplate.greeting.id;
    content.value = newTemplate.content;
    console.log(newTemplate)
  }
});
</script>

<template>
  <form @submit.prevent="addTemplate">
    <div>
      <label for="name1" class="template-label">Vorlage-Name</label><br>
      <input v-model="inputName" type="text" id="name" class="formTemplate"
             placeholder="Geben Sie einen Namen für die Vorlage ...">
    </div>
    <div>
      <label for="betreff1" class="template-label">Betreff</label><br>
      <input v-model="inputHeading" type="text" class="formTemplate"
             placeholder="Geben Sie einen Betreff für die Vorlage ..." id="betreff">
    </div>
    <div id="anredeBox">
      <label for="anrede" class="template-label">Anrede</label><br>
      <select id="anrede" class="formTemplate" v-model="selectedGreeting">
        <option disabled value="" id="placeholderSelectionTemplate">Wähle eine Anrede ...</option>
        <option v-for="item in greetingData" :key="item.content" :value="item.id">{{ item.content }}</option>
      </select>
    </div>
    <div class="editor-wrapper">
      <!--   !-->
      <QuillEditor id="editor" :toolbar="toolbarOptions" v-model:content="content" content-type="html"/>
    </div>
    <button type="submit">Absenden</button>
  </form>

</template>

<style scoped>
#anredeBox {
  margin-bottom: 3vh;
}

.editor-wrapper {
  max-width: 80vw; /* Maximale Breite 80% der Viewport-Breite */
  max-height: 35vh; /* Maximale Höhe 70% der Viewport-Höhe */
  overflow-y: auto; /* Vertikales Scrollen, wenn nötig */
}

/* Stile für den Quill Editor, falls nötig */
#editor {
  width: 100%; /* Vollständige Breite des Wrappers */
  height: 100%; /* Vollständige Höhe des Wrappers */
}
form {
  padding: 2% 3%;
}

.template-label {
  color: #5A5A5A;
  font-size: 0.8em;
}

.formTemplate::placeholder {
  color: #B3B3B3;
}

.formTemplate {
  all: unset;
  border: solid 1px #BEBEBE;
  border-radius: 5px;
  padding: 5px;
  width: 50vh;
  font-size: 0.5em;
}

#placeholderSelectionTemplate {
  color: #5A5A5A;
  font-size: 0.5em;
}
</style>