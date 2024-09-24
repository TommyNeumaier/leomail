<template>
  <div v-if="visible" class="mail-preview-modal">
    <div class="modal-content">
      <span class="close-button" @click="close">✕</span>

      <div v-if="currentEmailIndex !== null">
        <h3>Vorschau für <b>{{ selectedTemplate?.name }}</b></h3>

        <p class="indexing">{{ currentEmailIndex + 1 }} / {{ selectedUsers.length }}</p>

        <div class="info-row">
          <label>Betreff:</label>
          <input type="text" :value="selectedTemplate?.headline" disabled />
        </div>
        <div class="info-row">
          <label>Senden am:</label>
          <input type="text" :value="sendTime" disabled />
        </div>
        <div class="info-row">
          <label>An:</label>
          <input type="text" :value="selectedUsers[currentEmailIndex]?.firstName + ' ' + selectedUsers[currentEmailIndex]?.lastName + ` <${selectedUsers[currentEmailIndex]?.mailAddress}>`" disabled />
        </div>

        <div class="email-preview" v-html="filledTemplate"></div>

        <div class="navigation-buttons">
          <span class="arrow prev-arrow" @click="prevEmail" :class="{ 'disabled': currentEmailIndex === 0 }"></span>
          <span class="arrow next-arrow" @click="nextEmail" :class="{ 'disabled': currentEmailIndex === selectedUsers.length - 1 }"></span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, computed } from 'vue';
import type { Template, User } from '@/types';

const emit = defineEmits(['close']);

const props = defineProps({
  selectedTemplate: Object as () => Template | null,
  selectedUsers: Array as () => User[],
  personalized: Boolean,
  visible: Boolean,
  scheduledAt: String || null,
});

const currentEmailIndex = ref<number | null>(0);
const filledTemplate = ref<string>('');

const sendTime = computed(() => {
  return props.scheduledAt ? new Date(props.scheduledAt).toLocaleString() : 'Jetzt';
});

const fillTemplate = (template: Template, user: User) => {
  let filledContent = template.content
      .replace('{firstname}', `<span class="highlight">${user.firstName}</span>`)
      .replace('{lastname}', `<span class="highlight">${user.lastName}</span>`)
      .replace('{mailAddress}', `<span class="highlight">${user.mailAddress}</span>`);
  return filledContent;
};

// Schließen des Modals
const close = () => {
  currentEmailIndex.value = null;
  emit('close');
};

onMounted(() => {
  if (props.selectedTemplate && props.selectedUsers.length > 0) {
    filledTemplate.value = fillTemplate(props.selectedTemplate, props.selectedUsers[0]);
  }
});

watch([() => props.selectedTemplate, () => props.selectedUsers], ([newTemplate, newUsers]) => {
  if (newTemplate && newUsers.length > 0) {
    currentEmailIndex.value = 0;
    filledTemplate.value = fillTemplate(newTemplate, newUsers[0]);
  }
});

const prevEmail = () => {
  if (currentEmailIndex.value !== null && currentEmailIndex.value > 0) {
    currentEmailIndex.value--;
    filledTemplate.value = fillTemplate(props.selectedTemplate!, props.selectedUsers[currentEmailIndex.value]);
  }
};

const nextEmail = () => {
  if (currentEmailIndex.value !== null && currentEmailIndex.value < props.selectedUsers.length - 1) {
    currentEmailIndex.value++;
    filledTemplate.value = fillTemplate(props.selectedTemplate!, props.selectedUsers[currentEmailIndex.value]);
  }
};
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

.highlight {
  background-color: #ffd700;
  padding: 2px 5px;
  border-radius: 4px;
}
</style>