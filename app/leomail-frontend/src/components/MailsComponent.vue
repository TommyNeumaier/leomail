<script setup lang="ts">
import {computed, onMounted, ref} from 'vue'
import MailFormComponent from "@/components/MailFormComponent.vue";

const mails = ref([]);
const startIndex = ref(1);
const endIndex = ref(10);
const totalMails = ref(50);
const limit = ref(10);
const showEmailForm = ref(false);

const clickedEmailForm = () => {
  showEmailForm.value = true;
}

const decrement = () => {
  if (startIndex.value - limit.value >= 0) {
    startIndex.value -= limit.value;
    endIndex.value = startIndex.value + limit.value - 1;
  } else {
    startIndex.value = 1;
    endIndex.value = limit.value;
  }
};

const increment = () => {
  if (endIndex.value + limit.value <= totalMails.value) {
    startIndex.value += limit.value;
    endIndex.value = startIndex.value + limit.value - 1;
  } else {
  }
};

/*const displayedItems = computed(() => {
  return mails.value.slice(startIndex.value, endIndex.value + 1);
});

onMounted(() => {
  displayedItems;
})*/

</script>

<template>
  <div v-if="showEmailForm" id="form">
    <MailFormComponent/>
  </div>
  <div v-else id="bigVGContainer">
    <div id="VGHeaderBox">
      <h1 id="vgHeading">Emails</h1>

      <button id="neueMail" @click="clickedEmailForm">
        <p>Neue Email</p>
      </button>
    </div>


    <div id="search-container">
      <input type="text" id="search" placeholder="suche">
      <div id="searchIconBox">
        <img src="../assets/icons/search.png" alt="Suche" id="search-icon" width="auto" height="10">
      </div>
    </div>

    <div id="bigFeaturesContainer">
      <div id="mailFeaturesContainer">
        <div>
          <input type="checkbox" id="checkbox"/> <!--checkbox when all mails should be checked-->
        </div>
        <div>
          <img src="../assets/reload.png" alt="Reload" id="reload-icon" width="auto" height="12">
        </div>
        <div>
          <img src="../assets/trash.png" alt="Trash" id="trash-icon" width="auto" height="12">
        </div>
      </div>

      <div id="pages">
        <div id="pagesNummern">
          <p>{{ startIndex }}</p>
          <p>-</p>
          <p>{{ endIndex }}</p>
          <p>von</p>
          <div id="totalMailsBox">
            <p>{{ totalMails }}</p>
          </div>
        </div>
        <div id="pagesButtonBox">
          <button @click="decrement" :disabled="startIndex == 0" class="icon-button">
            <img src="../assets/icons/pfeil-links.png" alt="Decrement" class="icon">
          </button>
          <button @click="increment" :disabled="endIndex == totalMails - 1" class="icon-button">
            <img src="../assets/icons/pfeil-rechts.png" alt="Increment" class="icon">
          </button>
        </div>
      </div>
    </div>

    <div id="mailsBox">
      <div id="mailContentBox">
      </div>
    </div>
  </div>
</template>

<style scoped>
#form {
  width: 86vw;
  margin-top: 4vh;
  margin-left: 1.5%;
  display: flex;
  flex-direction: column;
}

#pagesNummern {
  display: flex;
  flex-direction: row;
  width: 70%;
  align-items: center;
  padding-left: 1%;
}

#pagesNummern p {
  font-size: 0.8rem;
  width: 12%;
  text-align: center;
}

#totalMailsBox {
  background-color: #ECECEC;
  border-radius: 8px;
  width: 35%;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-left: 10%;
}

#totalMailsBox p {
  width: 100%;
}

#bigFeaturesContainer {
  display: flex;
  flex-direction: row;
  width: 100%;
  height: 5%;
}

#pages {
  display: flex;
  flex-direction: row;
  width: 25%;
}

#mailFeaturesContainer {
  display: flex;
  flex-direction: row;
  width: 75%;
  padding-left: 1%;
}

#mailFeaturesContainer div {
  width: 4%;
}

#searchIconBox {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #ECECEC;
  height: 100%;
  width: 20%;
  border-radius: 5px;
}

#search-container {
  display: flex;
  flex-direction: row;
  width: 28%;
  height: 6%;
  border-radius: 5px;
  background-color: white;
  margin: 1.5% 0 1% 3%;
  box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.1);
}

#search {
  all: unset;
  width: 80%;
  padding-left: 3%;
  font-size: 0.8rem;
}

#bigVGContainer {
  width: 86.5%;
  margin-top: 2%;
  margin-left: 1.5%;
  display: flex;
  flex-direction: column;
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

#neueMail {
  all: unset;
  background-color: #E8E8E8;
  height: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 7px;
  margin-left: 80%;
  margin-top: 1.8%;
  box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.12);
  border: #8a8a8a solid 2px;
}

#neueMail:hover {
  background-color: #a2a2a2;
  color: white;
  border-color: #8a8a8a;
}

#neueMail p {
  padding: 0 20px;
  font-size: 1em;
  font-weight: 500;
}

#mailsBox {
  background-color: white;
  height: 75%;
  box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.12);

}

#mailFeaturesContainer {
  display: flex;
  flex-direction: row;
  width: 75%;
  padding-left: 1%;
}

#mailFeaturesContainer div {
  width: 4%;
}

#pagesButtonBox {
  width: 30%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-button {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
  margin: 0 2%;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  width: 30%;
}

.icon-button:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.icon-button:not(:disabled):hover {
  background-color: #f0f0f0;
  border-radius: 5px;
}

.icon-button:active {
  box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.2);
}

.icon {
  height: 70%;
  width: auto;
}
</style>