<template>
  <div id="mailDetailContainer" v-if="mailDetail">
    <div id="mailDetailHeader">
      <h1 id="heading">{{ mailDetail.meta.templateName }} - {{ mailDetail.meta.mailHeadline }}</h1>
    </div>

    <div id="content">
      <div class="info-row">
        <label>Gesendet von:</label>
        <input type="text" :value="senderName" disabled />
      </div>

      <div class="info-row">
        <label>Gesendet am:</label>
        <input type="text" :value="mailDetail.keyDates.sentOn" disabled />
      </div>

      <div v-if="recipients.length > 0" class="recipients-preview">
        <div class="info-row">
          <label>Betreff:</label>
          <input type="text" :value="mailDetail.meta.mailHeadline" disabled />
        </div>

        <div class="info-row">
          <label>An:</label>
          <input type="text" :value="recipientDisplay" disabled />
        </div>

        <div class="email-preview" v-html="filledTemplate"></div>

        <div class="navigation-buttons">
          <span class="arrow prev-arrow" @click="prevRecipient" :class="{ 'disabled': currentRecipientIndex === 0 }"></span>
          <p class="indexing">{{ currentRecipientIndex + 1 }} / {{ recipients.length }}</p>
          <span class="arrow next-arrow" @click="nextRecipient"
                :class="{ 'disabled': currentRecipientIndex === recipients.length - 1 }"></span>
        </div>
      </div>
    </div>
  </div>

  <div v-else>
    <p>Lade E-Mail...</p>
  </div>
</template>


<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { Service } from '@/services/service';
import { format, parseISO } from 'date-fns';

const route = useRoute();
const mailDetail = ref(null);
const senderName = ref('');
const recipients = ref([]);
const currentRecipientIndex = ref(0);
const filledTemplate = ref('');

// Initialize profile and email data
const mailId = ref(route.params.id);
const projectId = ref(route.params.projectId);

const getProfile = async () => {
  const response = await Service.getInstance().getProfile();
  senderName.value = `${response.data.firstName} ${response.data.lastName} <${response.data.mailAddress}>`;
};

// Fetch email details
const fetchMailDetail = async () => {
  const response = await Service.getInstance().getUsedTemplate(mailId.value, projectId.value);
  mailDetail.value = response.data;

  if (mailDetail.value && mailDetail.value.keyDates && mailDetail.value.keyDates.sentOn) {
    const sentOnDate = parseISO(mailDetail.value.keyDates.sentOn);
    mailDetail.value.keyDates.sentOn = format(sentOnDate, 'dd.MM.yyyy, HH:mm');
  }

  if (mailDetail.value && mailDetail.value.mails && mailDetail.value.mails.length > 0) {
    recipients.value = mailDetail.value.mails.map(mailItem => mailItem.contact);
    updateFilledTemplate();
  }
};

// Recipient navigation
const nextRecipient = () => {
  if (currentRecipientIndex.value < recipients.value.length - 1) {
    currentRecipientIndex.value++;
    updateFilledTemplate();
  }
};

const prevRecipient = () => {
  if (currentRecipientIndex.value > 0) {
    currentRecipientIndex.value--;
    updateFilledTemplate();
  }
};

// Fill template with greeting
const fillTemplate = (templateContent: string, greetingContent: string, recipient: any) => {
  // Replace placeholders in greeting
  let filledGreeting = greetingContent
      .replace(/{firstname}/g, recipient.firstName || '')
      .replace(/{lastname}/g, recipient.lastName || '')
      .replace(/{mailAddress}/g, recipient.mailAddress || '');

  // Replace placeholders in template content
  let filledContent = templateContent
      .replace(/{firstname}/g, recipient.firstName || '')
      .replace(/{lastname}/g, recipient.lastName || '')
      .replace(/{mailAddress}/g, recipient.mailAddress || '');

  // Combine greeting and content
  return `${filledGreeting}\n\n${filledContent}`;
};

// Update the preview
const updateFilledTemplate = () => {
  const currentMail = mailDetail.value.mails[currentRecipientIndex.value];

  if (currentMail && currentMail.content) {
    filledTemplate.value = currentMail.content;
  } else {
    filledTemplate.value = '';
  }
};

// Computed property for displayed recipient
const recipientDisplay = computed(() => {
  const recipient = recipients.value[currentRecipientIndex.value];
  return `${recipient.firstName} ${recipient.lastName} <${recipient.mailAddress}>`;
});

onMounted(() => {
  fetchMailDetail();
  getProfile();
});
</script>

<style scoped>
#mailDetailContainer {
  width: 86.5%;
  margin-top: 2%;
  margin-left: 1.5%;
  display: flex;
  flex-direction: column;
}

#mailDetailHeader {
  display: flex;
  flex-direction: row;
  background-color: white;
  box-shadow: 0 4px 4px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.12);
}

#heading {
  margin-left: 4%;
  margin-top: 2%;
  margin-bottom: 2%;
  font-size: 1.1em;
  font-weight: var(--font-weight-bold);
}

#content {
  margin-top: 2%;
  background-color: white;
  box-shadow: 5px 5px 10px lightgray;
  padding: 2% 3%;
  height: 90%;
}

.recipients-preview {
  margin-top: 20px;
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
  font-size: 1em;
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
  width: 10px;
  height: 10px;
  border: solid #78A6FF;
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
</style>


