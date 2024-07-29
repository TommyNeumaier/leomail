<script setup lang="ts">
import {computed, nextTick, onMounted, onUnmounted, ref, watch} from "vue";
import {Quill, QuillEditor} from "@vueup/vue-quill";
import {Service} from "@/services/service";
import '@vuepic/vue-datepicker/dist/main.css';
import {format} from "date-fns";

interface Template {
  id: number;
  name: string;
  headline: string;
  greeting: string;
  content: string;
  visible: boolean;
}

const filter = ref('');
const dropdownVisible = ref(false);
const selectedTemplate = ref<Template | null>(null);
const receiverInput = ref('');
const receiver = ref<number[]>([]);
const checked = ref(false);
const fetchedTemplates = ref<Template[]>([]);
const editor = ref<Quill | null>(null);

const closeDropdown = () => {
  dropdownVisible.value = false;
};

onMounted(() => {
  getTemplates()
  editor.value = new Quill('#editor');
  nextTick(() => {
    if (selectedTemplate.value) {
      onEditorReady(editor.value);
    }
  });

  document.addEventListener('click', handleClickOutside);
});

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside);
});

const handleClickOutside = (event: MouseEvent) => {
  const target = event.target as HTMLElement;
  const searchSelectBox = document.getElementById('searchSelectBox');
  if (searchSelectBox && !searchSelectBox.contains(target)) {
    closeDropdown();
  }
};

const date = ref({
  day: new Date().getDate().toString().padStart(2, "0"),
  month: (new Date().getMonth() + 1).toString().padStart(2, "0"),
  year: new Date().getFullYear()
});

const time = ref({
  hours: new Date().getHours().toString().padStart(2,"0"),
  minutes: new Date().getMinutes().toString().padStart(2,"0"),
});

const scheduledAt = ref({
  ...date.value,
  ...time.value
});

watch(date, (newDate) => {
  scheduledAt.value.year = newDate.getFullYear().toString();
  scheduledAt.value.month = (newDate.getMonth() + 1).toString().padStart(2, "0");
  scheduledAt.value.day = newDate.getDate().toString().padStart(2, "0");
});

watch(time, (newTime) => {
  scheduledAt.value.hours = newTime.getHours().toString().padStart(2,"0");
  scheduledAt.value.minutes = newTime.getMinutes().toString().padStart(2,"0")
});

const filterFunction = () => {
  const filterValue = filter.value.trim().toLowerCase();
  fetchedTemplates.value.forEach(item => {
    item.visible = item.name.toLowerCase().includes(filterValue);
  });
};

watch(filter, () => {
  filterFunction();
  dropdownVisible.value = filter.value.length > 0;
});

const showDropdown = () => {
  dropdownVisible.value = filter.value.length > 0;
};

const onEditorReady = (editor: Quill) => {
  if (selectedTemplate.value) {
    console.log(selectedTemplate.value.content);
    editor.root.innerHTML = selectedTemplate.value.content;
  }
};

const selectTemplate = (template: Template) => {
  selectedTemplate.value = template;
  filter.value = template.name;
  setTimeout(() => {
    closeDropdown();
  }, 0);

  if (editor.value) {
    nextTick(() => {
      onEditorReady(editor.value);
    });
  }
};

const getTemplates = async () => {
  const response = await Service.getInstance().getVorlagen();
  fetchedTemplates.value = response.data.map((template: { id: number; name: string; headline: string; greeting: string; content: string }) => ({
    ...template,
    visible: true
  }));
};

const parseReceiverInput = () => {
  receiver.value = receiverInput.value.split(',').map(Number);
};

const parseDate = () => {
  if(checked.value){
    return `${scheduledAt.value.year}-${scheduledAt.value.month}-${scheduledAt.value.day}T${scheduledAt.value.hours}:${scheduledAt.value.minutes}:00.000Z`;
  } else{
    return null;
  }
}

const sendMail = async () => {
  try {
    parseReceiverInput();
    console.log(receiver);
    const mailForm = {
      receiver: {
        contacts: receiver.value,
        groups: []
      },
      templateId: selectedTemplate.value?.id,
      personalized: true,  //todo: must be optimized if bauer wants the checkbox
      scheduledAt: parseDate(),
    };
    console.log(mailForm);
    console.log(parseDate());
    const response = await Service.getInstance().sendEmails(mailForm);
    console.log('Erfolgreich gesendet:', response.data);
    //emitEvents('template-added', formData);
  } catch (error) {
    console.error('Fehler beim Senden der Daten:', error);
  }
};

const handleSubmit = () => {
  sendMail();
}

</script>

<template>
  <div id="bigContainer">
    <div id="VGHeaderBox">
      <h1 id="vgHeading">Neue Email</h1>
    </div>
    <div id="formBox">
      <form>
        <div id="receiverFlexBox">
          <div class="boxLabel">
            <label for="an" class="mail-label">An:</label><br>
          </div>
          <input type="text" id="an" class="mailForm" v-model="receiverInput">
        </div>


        <div id="formFlexBox">
          <div id="dateFlexBox">
            <div class="boxLabel">
              <label class="mail-label">Senden am:</label></div>
            <br>
            <div id="checkboxSendLater">
              <input type="checkbox" id="date" v-model="checked">
              <label for="date">später senden</label>
            </div>
            <div id="datepickerFlexBox">
              <VueDatePicker v-if="checked" locale="de-AT" v-model="date" class="datepicker" id="datepicker"
                             now-button-label="Current" format="dd-mm-yyyy" :enable-time-picker="false"
                             placeholder='Date' date-picker :min-date="format(new Date(), 'yyyy-MM-dd')" ></VueDatePicker>
              <!--https://vue3datepicker.com/props/localization/-->
              <VueDatePicker v-if="checked" v-model="time" class="datepicker" time-picker placeholder='Time' :min-time="format(new Date(), 'HH:mm')" />
            </div>
          </div>

          <div id="templateBox">
            <div class="boxLabel">
              <label for="template" class="mail-label">Vorlagen:</label></div>
            <br>
            <div id="searchSelectBox">
              <div>
                <input type="text" id="myInput" v-model="filter" placeholder="Search.." @input="filterFunction"
                       @focus="showDropdown" class="mailForm">
              </div>
              <div v-if="dropdownVisible" class="dropdown-content">
                <a v-for="item in fetchedTemplates" :key="item.id" v-show="item.visible"
                   @click="selectTemplate(item)">{{ item.name }}</a>
              </div>
            </div>
          </div>
          <div class="editor-wrapper">
            <div id="editor" class="quill-editor"></div>
          </div>
        </div>
        <button type="button" @click="handleSubmit">Absenden</button>
      </form>
    </div>
  </div>
</template>

<style scoped>
#bigContainer{
  width: 86.5%;
  margin-top: 2%;
  margin-left: 1.5%;
  display: flex;
  flex-direction: column;
}
#date {
  margin-right: 2vw;
  margin-left: 1vw;
}

.boxLabel {
  width: 6vw;
}

#checkboxSendLater {
  width: 15vw;
}

#datepickerFlexBox {
  width: 50%;
  display: flex;
  flex-direction: row;
}

.datepicker {
  width: 8vw; /* Hier können Sie die gewünschte Schriftgröße angeben */
}

#templateBox {
  display: flex;
  flex-direction: row;
}

#receiverFlexBox {
  display: flex;
  flex-direction: row;
}

#dateFlexBox {
  display: flex;
  flex-direction: row;
  height: 5vh;
  margin-top: 1vh;
  margin-bottom: 1vh;
  align-items: center;
}

form {
  padding: 1% 3%;
}

.mail-label {
  color: #5A5A5A;
  font-size: 0.8em;
}

.mailForm::placeholder {
  color: #B3B3B3;
}

.mailForm {
  display: block;
  all: unset;
  border: solid 1px #BEBEBE;
  border-radius: 5px;
  padding: 0.6vw;
  width: 35%;
  font-size: 0.5em;
}

.mailForm:focus {
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2);
}

body {
  width: 100%;
}

#formBox {
  background-color: white;
  height: 87%;
  margin-top: 2vh;
  box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.12);
}

#VGHeaderBox {
  display: flex;
  flex-direction: row;
  background-color: white;
  box-shadow: 0 4px 4px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.12);
}

#vgHeading {
  margin-left: 4%;
  margin-top: 2%;
  margin-bottom: 2%;
  font-size: 1.1em;
}

#searchSelectBox {
  display: flex;
  flex-direction: column;
  border-radius: 20px;
  width: 35%;
}

#myInput {
  width: 100%;
}

/* Dropdown Content (Hidden by Default) */
.dropdown-content {
  width: 100%;
  border: 1px solid #ddd;
  z-index: 1;
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2);
  max-height: 25vh;
  overflow-y: scroll;
  cursor: pointer;
}

/* Links inside the dropdown */
.dropdown-content a {
  color: black;
  padding: 2% 4%;
  font-size: 0.8rem;
  text-decoration: none;
  display: block;
}

/* Change color of dropdown links on hover */
.dropdown-content a:hover {
  background-color: #f1f1f1
}

.editor-wrapper {
  border: black solid 1px;
  height: 90%;
}
</style>