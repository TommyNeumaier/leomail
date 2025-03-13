<script setup lang="ts">
import { onMounted, ref, watch, defineProps, defineEmits, defineExpose } from 'vue';
import Quill from 'quill';
import { Service } from '@/services/service';
import 'quill/dist/quill.snow.css';
import AutocompleteModule from '../../../autocompleteModule';
import { useAppStore } from '@/stores/app.store';

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
  filesRequired: boolean;
}

const appStore = useAppStore();
const inputName = ref('');
const inputHeading = ref('');
const selectedGreeting = ref<number | null>(null);
const content = ref('');
const greetingData = ref<Greeting[]>([]);
const filesRequired = ref(false);
const toolbarOptions = ref([
  [{ 'font': [] }],
  [{ 'size': ['small', false, 'large', 'huge'] }],
  ['bold', 'italic', 'underline', 'strike'],
  [{ 'color': [] }, { 'background': [] }],
  [{ 'align': [] }],
  [{ 'list': 'ordered' }, { 'list': 'bullet' }, { 'list': 'check' }],
  ['blockquote'],
  ['link', 'image', 'clean']
]);

const quillEditor = ref<Quill | null>(null);
const emit = defineEmits(['group-added', 'group-removed', 'group-saved']);
const props = defineProps<{ selectedTemplate: Template | null }>();
const localSelectedTemplate = ref<Template | null>(null);

const getGreetings = async () => {
  const response = await Service.getInstance().getGreetings();
  greetingData.value = response.data;
};

const addTemplate = async () => {
  const formData = {
    name: inputName.value,
    headline: inputHeading.value,
    content: content.value,
    greeting: selectedGreeting.value,
    projectId: appStore.$state.project,
    filesRequired: filesRequired.value
  };
  await Service.getInstance().addTemplate(formData);
  emit('group-added', formData);
  clearForm();
};

const updateTemplate = async () => {
  const updatedData = {
    id: localSelectedTemplate.value!.id,
    name: inputName.value,
    headline: inputHeading.value,
    content: content.value,
    filesRequired: filesRequired.value,
    greeting: selectedGreeting.value,
  };
  await Service.getInstance().updateTemplate(updatedData);
  emit('group-saved', updatedData);
  clearForm();
};

const removeTemplate = async () => {
  if (confirm('Möchten Sie diese Vorlage wirklich löschen?')) {
    await Service.getInstance().removeTemplate(localSelectedTemplate.value?.id, appStore.$state.project);
    emit('group-removed', localSelectedTemplate.value);
    clearForm();
  }
};

const clearForm = () => {
  inputName.value = '';
  inputHeading.value = '';
  selectedGreeting.value = null;
  filesRequired.value = false;
  content.value = '';
  quillEditor.value?.setText('');
  localSelectedTemplate.value = null;
};

defineExpose({ clearForm });

watch(
    () => props.selectedTemplate,
    (newTemplate) => {
      if (newTemplate) {
        inputName.value = newTemplate.name;
        inputHeading.value = newTemplate.headline;
        selectedGreeting.value = newTemplate.greeting.id;
        filesRequired.value = newTemplate.filesRequired;
        content.value = newTemplate.content;
        if (quillEditor.value) {
          quillEditor.value.root.innerHTML = newTemplate.content;
        }
        localSelectedTemplate.value = newTemplate;
      } else {
        clearForm();
      }
    },
    { immediate: true }
);

onMounted(async () => {
  const editor = new Quill('#editor', {
    theme: 'snow',
    modules: { toolbar: toolbarOptions.value }
  });
  quillEditor.value = editor;
  new AutocompleteModule(editor);
  editor.on('text-change', () => {
    content.value = editor.root.innerHTML;
  });

  await getGreetings();

  if (props.selectedTemplate) {
    localSelectedTemplate.value = props.selectedTemplate;
  }
});

watch(() => props.selectedTemplate, (newTemplate) => {
  if (newTemplate) {
    localSelectedTemplate.value = newTemplate;
    inputName.value = newTemplate.name;
    inputHeading.value = newTemplate.headline;
    selectedGreeting.value = newTemplate.greeting.id;
    filesRequired.value = newTemplate.filesRequired;
    content.value = newTemplate.content;
    if (quillEditor.value) {
      quillEditor.value.root.innerHTML = newTemplate.content;
    }
  } else {
    clearForm();
  }
});
</script>

<template>
  <form @submit.prevent="props.selectedTemplate ? updateTemplate() : addTemplate()">
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

    <div id="anhangBox">
      <label for="anhang" class="template-label">Anhang hinzufügen</label><br>
      <input type="checkbox" id="anhang" v-model="filesRequired">
    </div>

    <!--

    <div class="personContainer">
      <div class="personBox">
        <input
            type="checkbox"
            class="checkbox"
            id="individual"
            value="individual"
            v-model="checkedIndividual"
            @change="handleEntities('individual')"
        />
        <label for="individual">Privatperson</label>
      </div>

      <div class="personBox">
        <input
            type="checkbox"
            class="checkbox"
            id="company"
            value="company"
            v-model="checkedCompany"
            @change="handleEntities('company')"
        />
        <label for="company">Unternehmen</label>
      </div>
    </div>


    <img src="../../../assets/icons/icons8-info-250.png" id="tooltip" width="20"
         v-tooltip="{ value: 'Vorname: {firstname} Nachname: {lastname} Email-Adresse: {mailAddress}', showDelay: 200, hideDelay: 400 }"
         label="Save">
     -->
    <div class="editor-wrapper">
      <div id="editor" class="quill-editor"></div>
    </div>


    <div id="buttonBox">
      <button v-if="!selectedTemplate" type="submit" class="saveTemplate" :disabled="selectedTemplate != null">
        Erstellen
      </button>
      <button v-if="selectedTemplate" type="button" @click="removeTemplate()" class="saveTemplate" id="deleteButton"
              :disabled="selectedTemplate == null">Löschen
      </button>
      <button v-if="selectedTemplate" type="button" @click="updateTemplate()" class="saveTemplate"
              :disabled="selectedTemplate == null">Speichern
      </button>
    </div>
  </form>
</template>

<style scoped>
#anhangBox{
  display: flex;
  flex-direction: row;
  align-items: center;
  margin-bottom: 2vh;
}
#anhangBox label{
  padding: 0 0.5vw 0 0;
  font-size: 0.8em;
}

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
  margin-top: 2%;
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
  transition: background-color 0.2s ease, box-shadow 0.2s ease;
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
  margin-bottom: 2vh;
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
  font-size: 0.8em;
}

.formTemplate:focus {
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2);
}

#placeholderSelectionTemplate {
  color: #5A5A5A;
  font-size: 0.5em;
}

#deleteButton {
  background-color: white;
  border: none;
  color: #f5151c;
}

#deleteButton:hover {
  font-weight: var(--font-weight-bold);
  box-shadow: none;
}

/* checkbox
.personBox {
  display: flex;
  flex-direction: row;
  align-items: center;
  width: 15%;
}

.personBox label {
  margin-left: 5%;
}

.personContainer {
  display: flex;
  flex-direction: row;
  width: 100%;
}

#tooltip {
  display: flex;
  margin-left: 97%;
  margin-bottom: 0.2%;
}
*/
</style>

