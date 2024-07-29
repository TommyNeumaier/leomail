<script setup lang="ts">
import {onMounted, ref, watch} from 'vue';
import {Service} from '@/services/service';
import {Quill, QuillEditor} from '@vueup/vue-quill';
import {useAppStore} from "@/stores/app.store";

interface Greeting {
  id: number;
  content: string;
}

interface Template {
  id: number;
  name: string;
  headline: string;
  greeting: Greeting;
  content: string;
}

const appStore = useAppStore();
const inputName = ref('');
const inputHeading = ref('');
const selectedGreeting = ref('');
const content = ref('');
const greetingData = ref<Greeting[]>([]); // Ref-Typ für greetingData geändert
const toolbarOptions = ref([
  [{'font': []}],
  [{'size': ['small', false, 'large', 'huge']}],
  ['bold', 'italic', 'underline', 'strike'],
  [{'color': []}, {'background': []}],
  [{'align': []}],
  [{'list': 'ordered'}, {'list': 'bullet'}, {'list': 'check'}],
  ['blockquote'],
  ['link', 'image', 'clean']
]);
const quillEditor = ref<Quill | null>(null);
const emitEvents = defineEmits(['template-added', 'template-removed', 'template-saved']);
const props = defineProps<{ selectedTemplate: Template | null }>();


const getGreetings = async () => {
  const response = await Service.getInstance().getGreetings();
  greetingData.value = response.data;
};

const addTemplate = async () => {
  try {
    const formData = {
      name: inputName.value,
      headline: inputHeading.value,
      content: content.value,
      greeting: selectedGreeting.value,
      projectId: appStore.$state.project
    };
    console.log(formData);
    const response = await Service.getInstance().addTemplate(formData);
    console.log('Erfolgreich gesendet:', response.data);
    emitEvents('template-added', formData);
    clearForm();
  } catch (error) {
    console.error('Fehler beim Senden der Daten:', error);
  }
};

const updateTemplate = async () => {
  try {
    const updatedData = {
      id: props.selectedTemplate?.id,
      name: inputName.value,
      headline: inputHeading.value,
      content: content.value,
      greeting: selectedGreeting.value,
      projectId: appStore.$state.project
    };
    console.log(updatedData);
    const response = await Service.getInstance().updateTemplate(updatedData);
    console.log('Erfolgreich gesendet:', response.data);
    emitEvents('template-saved', updatedData)
    clearForm();
  } catch (error) {
    console.error('Fehler beim Speichern der Daten:', error);
  }
};

const removeTemplate = async () => {
  const confirmed = confirm('Möchten Sie diese Vorlage wirklich löschen?');
  if (confirmed) {
    try {
      const response = await Service.getInstance().removeTemplate(props.selectedTemplate?.id, appStore.$state.project);
      console.log('Erfolgreich gesendet:', response.data);
      emitEvents('template-removed', props.selectedTemplate);
      clearForm();
    } catch (error) {
      console.error('Fehler beim Löschen der Daten:', error);
    }
  }
};

const clearForm = () => {
  inputName.value = '';
  inputHeading.value = '';
  selectedGreeting.value = '';
  content.value = '';
  if (quillEditor.value) {
    quillEditor.value.setContents('');
  }
};

onMounted(() => {
  getGreetings();
});

watch(
    () => props.selectedTemplate,
    (newTemplate) => {
      console.log('selected Temp');
      console.log(props.selectedTemplate);
      clearForm();
      if (newTemplate) {
        inputName.value = newTemplate.name;
        inputHeading.value = newTemplate.headline;
        selectedGreeting.value = newTemplate.greeting;
        console.log(selectedGreeting.value)
        content.value = newTemplate.content;
        if (quillEditor.value) {
          quillEditor.value.root.innerHTML = newTemplate.content;
        }
        console.log('newTemplate' + newTemplate.id);
      }
    }
);

const onEditorReady = (editor: Quill) => {
  quillEditor.value = editor;
  editor.on('text-change', () => {
    content.value = editor.root.innerHTML;
  });
};

onMounted(() => {
  const editor = new Quill('#editor', {theme: 'snow', modules: {toolbar: toolbarOptions.value}});
  onEditorReady(editor);
});
</script>

<template>
  <form @submit.prevent="addTemplate">
    <div>
      <label for="name" class="template-label">Vorlage-Name</label><br>
      <input v-model="inputName" type="text" id="name" class="formTemplate"
             placeholder="Geben Sie einen Namen für die Vorlage ..." required>
    </div>
    <div id="formFlexBox">
      <div>
        <label for="betreff" class="template-label">Betreff</label><br>
        <input v-model="inputHeading" type="text" id="betreff" class="formTemplate"
               placeholder="Geben Sie einen Betreff für die Vorlage ..." required>
      </div>
      <div id="anredeBox">
        <label for="anrede" class="template-label">Anrede</label><br>
        <select id="anrede" class="formTemplate" v-model="selectedGreeting" required>
          <option disabled value="">Wähle eine Anrede ...</option>
          <option v-for="item in greetingData" :key="item.id" :value="item.id">{{ item.content }}</option>
        </select>
      </div>
    </div>
    <div class="editor-wrapper">
      <div id="editor" class="quill-editor"></div>
    </div>

    <img src="../assets/icons/icons8-info-250.png" width="20" v-tooltip="{ value: '{{ firstname }}', showDelay: 200, hideDelay: 400 }" label="Save">

    <div id="buttonBox">
      <button v-if="!selectedTemplate" type="submit" class="saveTemplate" :disabled="selectedTemplate != null">
        Erstellen
      </button>
      <button v-if="selectedTemplate" type="button" @click=removeTemplate class="saveTemplate" id="deleteButton"
              :disabled="selectedTemplate == null">Löschen
      </button>
      <button v-if="selectedTemplate" type="button" @click=updateTemplate class="saveTemplate"
              :disabled="selectedTemplate == null">Speichern
      </button>
    </div>
  </form>
</template>

<style scoped>

#formFlexBox {
  display: flex;
  flex-direction: row;
  margin-top: 1%;
  width: 100%;
}

#formFlexBox div {
  width: 40%;
}

#formFlexBox div .formTemplate {
  width: 100%;
}

#buttonBox {
  display: flex;
  flex-wrap: wrap;
  width: 25%;
  margin-top: 2%;
  margin-left: 51vw;
  justify-content: flex-end; /* Align items to the right */
}

.saveTemplate {
  all: unset;
  padding: 1vh 3vh;
  border-radius: 12px;
  background-color: #78A6FF;
  color: white;
  border: #78A6FF solid 1px;
  font-size: 0.8rem;
  margin-left: 2%;
}

.saveTemplate:hover {
  background-color: rgba(75, 129, 253, 0.86);
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2);
}

.saveTemplate:disabled {
  background-color: lightgray;
  border-color: lightgray;;
}

.saveTemplate:disabled:hover {
  border-color: lightgray;
  box-shadow: none;
}

#anredeBox {
  margin-bottom: 3vh;
  margin-left: 5%
}

.editor-wrapper {
  max-width: 80vw;
  max-height: 35vh;
  overflow-y: auto;
}

#editor {
  width: auto;
  height: 25vh;
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
  display: block;
  all: unset;
  border: solid 1px #BEBEBE;
  border-radius: 5px;
  padding: 0.6vw;
  width: 40%;
  font-size: 0.5em;
}

.formTemplate:focus {
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2);
}

#placeholderSelectionTemplate {
  color: #5A5A5A;
  font-size: 0.5em;
}

#deleteButton {
  background-color: #f5151c;
  color: white;
  border: #ff393f solid 1px;
}

#deleteButton:hover {
  background-color: rgba(253, 75, 96, 0.86);
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2);
}
</style>

