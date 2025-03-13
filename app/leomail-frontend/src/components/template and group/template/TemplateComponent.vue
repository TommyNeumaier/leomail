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