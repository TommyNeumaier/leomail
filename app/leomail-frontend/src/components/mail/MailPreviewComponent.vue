<template>
  <div v-if="visible" class="mail-preview-modal">
    <div class="modal-content">
      <span class="close-button" @click="close">✕</span>

      <div v-if="allRecipients.length > 0">
        <h3>Vorschau für <b>{{ selectedTemplate?.name }}</b></h3>

        <p class="indexing">{{ currentEmailIndex + 1 }} / {{ allRecipients.length }}</p>

        <div class="info-row">
          <label>Betreff:</label>
          <input type="text" :value="selectedTemplate?.headline" disabled />
        </div>
        <div class="info-row">
          <label>Senden am:</label>
          <input type="text" :value="formattedSendTime" disabled />
        </div>
        <div class="info-row">
          <label>An:</label>
          <input type="text" :value="recipientDisplay" disabled/>
        </div>

        <div class="email-preview" v-html="filledTemplate"></div>

        <div class="navigation-buttons">
          <span class="arrow prev-arrow" @click="prevEmail" :class="{ 'disabled': currentEmailIndex === 0 }"></span>
          <span class="arrow next-arrow" @click="nextEmail"
                :class="{ 'disabled': currentEmailIndex === allRecipients.length - 1 }"></span>
        </div>

        <div class="checkbox">
          <input type="checkbox" v-model="confirmed" id="confirmation" />
          <label for="confirmation">Ich bestätige, dass die Vorschau korrekt ist</label>
        </div>

        <button type="button" @click="emitSendMail" :disabled="!confirmed" class="send-button">Absenden</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {computed, onMounted, ref, watch} from 'vue';
import type {Template, User} from '@/types';

const emit = defineEmits(['close', 'send-mail']);

const props = defineProps<{
  selectedTemplate: Template | null;
  selectedUsers: User[];
  visible: boolean;
  personalized: boolean;
  scheduledAt: string | null;
}>();

const currentEmailIndex = ref<number>(0);
const filledTemplate = ref<string>('');
const confirmed = ref(false);
const allRecipients = ref<User[]>([]);

const formattedSendTime = computed(() => {
  if (props.scheduledAt) {
    return new Date(props.scheduledAt).toLocaleString();
  }
  return 'Jetzt';
});

const fillTemplate = (template: Template, user: User) => {
  if (!template || !user) {
    console.error('Template oder User ist undefined');
    return '';
  }
  return template.content
      .replace(/{firstname}/g, `<span class="highlight">${user.firstName}</span>`)
      .replace(/{lastname}/g, `<span class="highlight">${user.lastName}</span>`)
      .replace(/{mailAddress}/g, `<span class="highlight">${user.mailAddress}</span>`);
};

const updateFilledTemplate = () => {
  if (currentEmailIndex.value < allRecipients.value.length) {
    const user = allRecipients.value[currentEmailIndex.value];
    filledTemplate.value = fillTemplate(props.selectedTemplate!, user);
    console.log('Filled Template in Preview:', filledTemplate.value); // Debugging
  }
};

const nextEmail = () => {
  if (currentEmailIndex.value < allRecipients.value.length - 1) {
    currentEmailIndex.value++;
    updateFilledTemplate();
  }
};

const prevEmail = () => {
  if (currentEmailIndex.value > 0) {
    currentEmailIndex.value--;
    updateFilledTemplate();
  }
};

const loadRecipients = () => {
  allRecipients.value = props.selectedUsers;
  if (props.selectedTemplate && allRecipients.value.length > 0) {
    filledTemplate.value = fillTemplate(props.selectedTemplate, allRecipients.value[0]);
    currentEmailIndex.value = 0;
    console.log('Initial Filled Template in Preview:', filledTemplate.value); // Debugging
  }
};

watch(() => props.visible, (newValue) => {
  if (newValue) {
    loadRecipients();
    console.log(allRecipients.value)
  }
});


const recipientDisplay = computed(() => {
  if (currentEmailIndex.value >= allRecipients.value.length) return '';
  const user = allRecipients.value[currentEmailIndex.value];
  return `${user.firstName} ${user.lastName} <${user.mailAddress}>`;
});

const close = () => {
  // Reset the index and filled template
  currentEmailIndex.value = 0;
  filledTemplate.value = '';
  emit('close');
};

const emitSendMail = () => {
  if (confirmed.value) {
    emit('send-mail');
  }
};

onMounted(() => {
  loadRecipients();
  if (allRecipients.value.length > 0) {
    currentEmailIndex.value = 0;
    updateFilledTemplate();
  }
});
</script>

<style scoped>
.mail-preview-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 30px;
  width: 70vw;
  height: 80vh;
  border-radius: 12px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  color: #333;
}

.close-button {
  position: absolute;
  top: 15px;
  right: 20px;
  font-size: 1.8em;
  cursor: pointer;
}

h3 {
  text-align: center;
  font-size: 1.5em;
  font-weight: 600;
  color: #333;
}

.indexing {
  text-align: center;
  font-size: 0.9em;
  margin-bottom: 10px;
  color: #555;
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.info-row label {
  width: 120px;
  font-weight: bold;
  color: #333;
}

.info-row input {
  flex: 1;
  padding: 5px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background-color: #f9f9f9;
  color: #333;
}

.email-preview {
  margin-top: 20px;
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background-color: #f9f9f9;
  min-height: 40vh;
  overflow-y: auto;
  color: #333;
}

.navigation-buttons {
  display: flex;
  justify-content: space-between;
  margin-top: 20px;
  margin-bottom: 20px;
}

.arrow {
  display: inline-block;
  width: 30px;
  height: 30px;
  border: solid black;
  border-width: 0 3px 3px 0;
  padding: 5px;
  cursor: pointer;
  transform: rotate(45deg);
}

.prev-arrow {
  transform: rotate(135deg);
}

.next-arrow {
  transform: rotate(-45deg);
}

.arrow.disabled {
  border-color: #cccccc;
  cursor: not-allowed;
}

.checkbox {
  margin-top: 20px;
}

.checkbox input {
  margin-right: 10px;
}

button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.send-button {
  background-color: #4CAF50;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 5px;
  font-size: 1em;
  cursor: pointer;
  align-self: center;
}

.send-button:hover {
  background-color: #45a049;
}

.highlight {
  background-color: #ffd700;
  padding: 2px 5px;
  border-radius: 4px;
}
</style>