<script setup lang="ts">
import {computed, onMounted, ref, watch} from "vue";
import {Quill, QuillEditor} from "@vueup/vue-quill";
import {Service} from "@/stores/service";
import '@vuepic/vue-datepicker/dist/main.css';

interface DropdownItem {
  id: number;
  label: string;
  visible: boolean;
}

const items: DropdownItem[] = [
  {id: 1, label: 'About', visible: true},
  {id: 2, label: 'Base', visible: true},
  {id: 3, label: 'Blog', visible: true},
  {id: 4, label: 'Contact', visible: true},
  {id: 5, label: 'Custom', visible: true},
  {id: 6, label: 'Support', visible: true},
  {id: 7, label: 'Tools', visible: true},
];

const toolbarOptions = ref([]);
const quillEditor = ref<Quill | null>(null);

const filter = ref('');
const dropdownVisible = ref(false);
const filteredItems = ref(items);
const selectedTemplate = ref<DropdownItem | null>(null);
const receiver = ref([]);
const checked = ref(false);

const date = ref({
  day: new Date().getDate(),
  month: new Date().getMonth() + 1, // Monate sind 0-basiert, daher +1
  year: new Date().getFullYear()
});

const time = ref({
  hours: new Date().getHours(),
  minutes: new Date().getMinutes()
});

const scheduledAt = ref({
  ...date.value,
  ...time.value
});

const filterFunction = () => {
  const filterValue = filter.value.trim().toLowerCase();
  items.forEach(item => {
    const txtValue = item.label.toLowerCase();
    item.visible = txtValue.includes(filterValue);
  });
};

watch(filter, () => {
  filterFunction();
});

const showDropdown = () => {
  dropdownVisible.value = true;
};

const selectTemplate = (template: DropdownItem) => {
  selectedTemplate.value = template;
  dropdownVisible.value = false;
  filter.value = selectedTemplate.value.label;
};

const checkIfAlreaySelected = () => {
  filter.value = '';
}

let htmlContent = '<p>This is <h1>HTML</h1> content.</p>';

const onEditorReady = (editor: Quill) => {
  editor.root.innerHTML = htmlContent; // HTML-Inhalt in den Editor einfügen
};

onMounted(() => {
  const editor = new Quill('#editor');
  onEditorReady(editor);
});

const sendMail = async () => {
  try {
    console.log(receiver);
    const mailForm = {
      receiver: receiver.value,
      templateId: selectedTemplate.value?.id,
      personalized: true,  //todo: must be optimized if bauer wants the checkbox
      scheduledAt: scheduledAt.value,
    };
    console.log(mailForm);
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
          <input type="text" id="an" class="mailForm" v-model="receiver">
        </div>


        <div id="formFlexBox">
          <div id="dateFlexBox">
            <div class="boxLabel">
              <label class="mail-label">Senden am:</label></div><br>
            <div id="checkboxSendLater">
              <input type="checkbox" id="date" v-model="checked">
              <label for="date">später senden</label>
            </div>
            <div id="datepickerFlexBox">
              <VueDatePicker v-if="checked" locale="de-AT" v-model="date" class="datepicker" id="datepicker"
                             now-button-label="Current" format="dd-mm-yyyy" :enable-time-picker="false"
                             placeholder='Date' date-picker></VueDatePicker>
              <!--https://vue3datepicker.com/props/localization/-->
              <VueDatePicker v-if="checked" v-model="time" class="datepicker" time-picker placeholder='Time'/>
            </div>
          </div>

          <div id="templateBox">
            <div class="boxLabel">
              <label for="template" class="mail-label">Vorlagen:</label></div><br>
            <div id="searchSelectBox">
              <div><input type="text" id="myInput" v-model="filter" placeholder="Search.." @input="filterFunction"
                          @focus="showDropdown" @click="checkIfAlreaySelected" class="mailForm"></div>
              <div v-if="dropdownVisible" class="dropdown-content">
                <a v-for="item in filteredItems" :key="item.id" v-show="item.visible"
                   @click="selectTemplate(item)">{{ item.label }}</a>
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
#date{
  margin-right: 2vw;
  margin-left: 1vw;
}
.boxLabel{
  width: 6vw;
}
#checkboxSendLater{
  width: 15vw;
}
#searchSelectBox {

}

#datepickerFlexBox {
  width: 50%;
  display: flex;
  flex-direction: row;
}

.datepicker{
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

#bigContainer {
  height: 100%;
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
  height: 85%;
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