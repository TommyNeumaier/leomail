<script setup lang="ts">

import HeaderComponent from "@/components/HeaderComponent.vue";
import PersonenFormComponent from "@/components/ContactFormComponent.vue";
</script>

<template>
  <header-component></header-component>
  <div id="bigContainer">
    <div id="listContainer">
      <h3 id="headline">Personen</h3>
      <div id="search-container">
        <div id="searchIconBox">
          <img src="../assets/icons/search.png" alt="Suche" id="search-icon" width="auto" height="10">
        </div>
        <input type="text" id="search" placeholder="suche">
      </div>
    </div>

    <div id="contentContainer">
      <personen-form-component></personen-form-component>
    </div>
  </div>
</template>

<style scoped>
#headline{
  font-weight: var(--font-weight-medium);
  padding: 2vh 0 1vh 1.5vw;
}

#searchIconBox {
  display: flex;
  align-items: center;
  justify-content: center;
}

#search-container {
  display: flex;
  flex-direction: row;
  width: 90%;
  margin: auto;
  border-radius: 5px;
  padding: 2% 4%;
  background-color: #ECECEC;
}

#search {
  all: unset;
  width: 90%;
  margin-left: 3%;
  font-size: 0.3rem;
}

#bigContainer{
  width: 80%;
  height: 80vh;
  display: flex;
  flex-direction: row;
  margin: auto;
  margin-top: 4vh;
  box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.12);
}
#listContainer{
  width: 25%;
  border-right: rgba(0, 0, 0, 0.20) solid 2px;
}
#contentContainer{
  width: 75%;
}
</style>