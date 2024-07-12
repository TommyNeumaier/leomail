<script setup lang="ts">
import NavComponent from "@/components/NavComponent.vue";
import axios from "axios";
import {onMounted, ref, watch} from "vue";
import {Service} from "@/stores/service";
import VorlageMainContentComponent from "@/components/VorlageMainContentComponent.vue";
import {useRoute} from "vue-router";

const route = useRoute();

const fetchedData = ref<{ name: string }[]>([]);
const selectedTemplate = ref<{ name: string, headline: string, greeting: string } | null>(null);
let headlineVG = ref('');
let newVGButton = ref('');
let formVG = ref(null);

const getData = async () => {
  let response;
  if (route.path.includes('vorlagen')) {
    response = await Service.getInstance().getVorlagen();
  } else if (route.path.includes('gruppen')) {
    response = await Service.getInstance().getGruppen();
  }
  fetchedData.value = response.data;
};

const handleClick = (item: { name: string, headline: string, greeting: string }, index: number) => {
  console.log(item); // Gibt die Daten des geklickten Objekts aus
  console.log(index)
  selectedTemplate.value = item;

  const vorlagenEintrag = document.querySelector('#template-' + index) as HTMLElement | null;
  if (vorlagenEintrag) {
    for(let i = 0; i < document.getElementsByClassName("vorlagenItems").length; i++) {
      let entry = document.getElementById(`template-${i}`) as HTMLElement
      entry.style.fontWeight = "normal"
    }

    vorlagenEintrag.style.fontWeight = 'bold'; // Beispiel: Rand ändern
  }
};

const emittedVorlagen = ref<{name:string}[]>([]);

const handleVorlageAdded = (newVorlage: {name:string}) => {
  emittedVorlagen.value.push(newVorlage);
  getData();
}

onMounted(() => {
  getData()
})

watch(
    () => route.path,
    (newPath) => {
      if (newPath.toLowerCase().includes('vorlagen')) {
        headlineVG.value = 'Vorlagen';
        newVGButton.value = 'Neue Vorlage';
        formVG.value = VorlageMainContentComponent;
      } else if (newPath.toLowerCase().includes('gruppen')) {
        headlineVG.value = 'Gruppen';
        newVGButton.value = 'Neue Gruppe';
        formVG.value = null;
      }
      getData();
    },
    { immediate: true }
);
</script>

<template>
  <div id="VGMainContainer">
    <nav-component></nav-component>

    <div id="bigVGContainer">

      <div id="dataVGContainer">
        <h3 id="headlineDataVG">{{ headlineVG }}</h3>

        <div id="getVGBox">
          <!--suche nach Vorlagen-->
          <div id="search-container">
            <div id="searchIconBox">
              <img src="../assets/icons/search.png" alt="Suche" id="search-icon" width="auto" height="10">
            </div>
            <input type="text" id="search" placeholder="suche">
          </div>

          <ul id="vorlagenBoxContainer">
            <li v-for="(item, index) in fetchedData" :key="index" :id="String('template-' + index)" @click="handleClick(item,index)" class="vorlagenItems">
              {{ item.name }}
            </li>
          </ul>

        </div>

        <div id="newVGBox">
          <button id="newVGButton">
            <!--<img src="../assets/icons/newMail-grau.svg" width="auto" height="10">-->
            <p>{{newVGButton}}</p>
          </button>
        </div>
      </div>

      <div id="contentVGContainer">
        <div id="VGHeaderBox">
          <h1 id="vgHeading">{{ headlineVG }}</h1>
        </div>

        <div id="VGFormBox">
          <component :is="formVG" @template-added="handleVorlageAdded" :selected-template="selectedTemplate"></component>
        </div>
      </div>

    </div>
  </div>
</template>

<style scoped>
#vorlagenBoxContainer{
  margin-top: 5%;
}
.vorlagenItems{
  font-size: 0.7em;
}
.vorlagenItems:hover{
  color: darkgray;
  cursor: pointer;
}

#VGMainContainer {
  display: flex;
  flex-direction: row;
}

#bigVGContainer {
  width: 90%;
  margin-top: 2%;
  padding-left: 0.3%;
  padding-right: 2%;
  display: flex;
  flex-direction: row;
}

#newVGButton {
  all: unset; /* Entfernt alle CSS-Eigenschaften */
  color: #A3A3A3;
  font-weight: bold;
}

#newVGButton p {
  font-size: 0.8em;
}

#dataVGContainer {
  width: 15%;
  display: flex;
  flex-direction: column;
}

#headlineDataVG {
  margin-top: 5%;
  margin-left: 5%;
  font-size: 1.1em;
  text-align: left;
}

#getVGBox {
  height: 60%;
}

#newVGBox {
  height: 40%;
  width: 85%;
  border-top: #A3A3A3 solid 2px;
  margin-left: 7.5%;
}

#contentVGContainer {
  width: 85%;
  padding-left: 1%;
  padding-right: 2%;
}

input[type="text"] {
  all: unset; /* Entfernt alle CSS-Eigenschaften */
  width: 60%;
}

#searchIconBox {
  display: flex;
  align-items: center;
  justify-content: center;
}

#search-container {
  display: flex;
  flex-direction: row;
  margin-top: 3%;
  margin-left: 1%;
  margin-right: 1%;
  border-radius: 5px;
  padding: 2% 4%;
  background-color: #ECECEC;
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2);
}

#search {
  width: 90%;
  margin-left: 3%;
  font-size: 0.2em;
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

#VGFormBox {
  height: 80%;
  margin-top: 5%;
  background-color: white;
  box-shadow: 5px 5px 10px lightgray;
}
</style>