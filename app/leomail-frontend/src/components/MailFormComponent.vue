<script setup lang="ts">
import {ref, watch} from "vue";

interface DropdownItem {
  id: number;
  label: string;
  visible: boolean;
}

const items: DropdownItem[] = [
  { id: 1, label: 'About', visible: true },
  { id: 2, label: 'Base', visible: true },
  { id: 3, label: 'Blog', visible: true },
  { id: 4, label: 'Contact', visible: true },
  { id: 5, label: 'Custom', visible: true },
  { id: 6, label: 'Support', visible: true },
  { id: 7, label: 'Tools', visible: true },
];

const filter = ref('');
const dropdownVisible = ref(false);
const filteredItems = ref(items);
const selectedTemplate = ref<DropdownItem | null>(null);

const date = ref({
  day: new Date().getDate(),
  month: new Date().getMonth() + 1, // Monate sind 0-basiert, daher +1
  year: new Date().getFullYear()
});

const time = ref({
  hours: new Date().getHours(),
  minutes: new Date().getMinutes()
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

</script>

<template>
  <form>
    <div>
      <label for="an" class="mail-label">An</label><br>
      <input type="text" id="an" class="formMail">
    </div>
    <div id="formFlexBox">
      <div>
        <label class="mail-label">Senden am:</label><br>
        <input type="checkbox" id="date" class="formMail">
        <label for="date">später senden</label>
        <VueDatePicker locale="de-AT" v-model="date"
                       now-button-label="Current" format="dd-mm-yyyy" :enable-time-picker="false"
                       placeholder='Select Date' date-picker></VueDatePicker>
        <!--https://vue3datepicker.com/props/localization/-->
        <VueDatePicker v-model="time" time-picker placeholder='Select Time'/>
      </div>

      <div id="templateBox">
        <label for="template" class="mail-label">Vorlagen</label><br>

        <div id="searchSelectBox">
          <div><input type="text" id="myInput" v-model="filter" placeholder="Search.." @input="filterFunction" @focus="showDropdown" @click="checkIfAlreaySelected"></div>
          <div v-if="dropdownVisible" class="dropdown-content">
            <a v-for="item in filteredItems" :key="item.id" v-show="item.visible" @click="selectTemplate(item)">{{ item.label }}</a>
          </div>
        </div>

      </div>
    </div>
  </form>
</template>

<style scoped>
#searchSelectBox{
  display: flex;
  flex-direction: column;
  border-radius: 20px;
}

#myInput{
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
.dropdown-content a:hover {background-color: #f1f1f1}
</style>