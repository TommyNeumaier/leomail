<script setup lang="ts">
import { ref } from 'vue'
import Example from "@/components/Example.vue";

</script>

<template>
  <div id="bigMainBox">
    <div id="mailHeaderBox">
      <h1 id="mail-heading">Emails</h1>

      <div id="neueMail">
        <p>Neue Email</p>
      </div>
    </div>

    <div id="search-container">
      <input type="text" id="search" placeholder="Suche">
      <button id="search-button">
        <img src="../assets/lupe.png" alt="Suche" id="search-icon" width="auto" height="20">
      </button>
    </div>

    <div id="mailFeaturesContainer">
      <input type="checkbox" id="checkbox"/> <!--checkbox when all mails should be checked-->
      <img src="../assets/reload.png" alt="Reload" id="reload-icon" width="auto" height="12">
      <img src="../assets/trash.png" alt="Trash" id="trash-icon" width="auto" height="12">
    </div>

    <div id="mailsBox">
      <div id="mailContentBox">
        <example></example>
      </div>
    </div>
  </div>
</template>

<style scoped>
#search-container{
  display: flex;
  flex-direction: row;
  margin-top: 1%;
}
#search{
  background-color: white;
  border-color: white;
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2);
  width: 20%;
  border-top-left-radius: 5px;
  border-bottom-left-radius: 5px;
}
#search-button{
  background-color: #ECECEC;
  border-color: #ECECEC;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 5px;
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2);
}
#search-icon{
  padding: 15%;
}
#bigMainBox{
  width: 86.5%;
  margin-top: 2%;
  margin-left: 1.5%;
  display: flex;
  flex-direction: column;
}
#mailHeaderBox{
  display: flex;
  flex-direction: row;
  height: 12%;
  background-color: white;
  box-shadow: 0 4px 4px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.12);
}
#mail-heading{
  margin-left: 4%;
  margin-top: 2%;
  font-size: 1.1em;
}
#neueMail{
  background-color: #E8E8E8;
  height: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 7px;
  margin-left: 80%;
  margin-top: 1.8%;
  box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.12);
}
#neueMail p{
  padding: 0 20px;
  font-size: 1em;
  font-weight: 500;
}
#mailsBox{
  background-color: white;
  height: 75%;
  box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.12);

}
</style>