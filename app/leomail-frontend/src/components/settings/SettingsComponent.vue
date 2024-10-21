<script setup lang="ts">
// Your script content (if needed)
</script>

<template>
  <div id="bigContentBox">
    <form @submit.prevent="saveForm">
      <!-- Projektname und Anzeigename -->
      <div class="inputRow">
        <div class="inputBox">
          <label for="projectname">Projektname</label>
          <input
              type="text"
              id="projectname"
              class="project-form"
              v-model="projectName"
              placeholder="Projektname eingeben"
          />
        </div>

        <div class="inputBox">
          <label for="displayName">Anzeigename</label>
          <input
              type="text"
              id="displayName"
              class="project-form"
              v-model="displayName"
              placeholder="Anzeigename eingeben"
          />
        </div>
      </div>

      <!-- Kurzbeschreibung -->
      <div class="inputBox">
        <label for="description">Kurzbeschreibung</label>
        <textarea
            id="description"
            class="project-form"
            v-model="description"
            placeholder="Kurzbeschreibung eingeben"
        ></textarea>
      </div>

      <!-- Mail-Adresse und Passwort -->
      <div class="inputRow">
        <div class="inputBox">
          <label for="mail">Mail-Adresse</label>
          <input
              type="email"
              id="mail"
              class="project-form"
              v-model="mail"
              placeholder="Mail-Adresse eingeben"
          />
        </div>

        <div class="inputBox">
          <label for="password">Passwort</label>
          <input
              type="password"
              id="password"
              class="project-form"
              v-model="password"
              placeholder="Passwort eingeben"
          />
        </div>
      </div>

      <!-- Benutzer (recipients) -->
      <div class="inputBox">
        <label for="recipients">Benutzer</label>
        <div class="project-form"></div>
      </div>

      <!-- Save Button -->
      <div class="buttonBox">
        <button type="submit" class="save">Speichern</button>
      </div>
    </form>
  </div>
</template>


<style scoped>
/* Container */
#bigContentBox {
  width: 90%;
  margin: 2% auto;
  padding: 3rem 4rem;
  display: flex;
  flex-direction: column;
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
}

/* Flexbox row for input pairs */
.inputRow {
  display: flex;
  justify-content: space-between;
  gap: 2rem;
}

/* Input container */
.inputBox {
  flex: 1;
  display: flex;
  flex-direction: column;
  margin-bottom: 1.5em;
}

/* Labels */
.inputBox label {
  margin-bottom: 0.5rem;
  font-size: 1rem;
  color: #333;
}

/* Inputs and textareas */
.project-form {
  display: block;
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 1rem;
  background-color: #fff; /* Set background to white */
  color: #333; /* Default text color */
  transition: border-color 0.3s ease;
}

/* Placeholder color */
.project-form::placeholder {
  color: #999; /* Grayish placeholder text */
  //font-style: italic;
}

/* Focus state */
.project-form:focus {
  border-color: #78a6ff;
  outline: none;
  background-color: #fff;
}

/* Textarea specific */
textarea.project-form {
  min-height: 100px;
  resize: vertical;
}

/* Button container */
.buttonBox {
  display: flex;
  justify-content: flex-end;
  margin-top: 2rem;
}

/* Save button */
.save {
  padding: 0.75rem 2rem;
  background-color: #78a6ff;
  color: white;
  font-size: 1rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.3s ease, box-shadow 0.3s ease;
}

.save:hover {
  background-color: #4b81fd;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

/* Disabled state */
.save:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

/* Responsive adjustments */
@media (max-width: 600px) {
  .inputRow {
    flex-direction: column;
  }

  .project-form {
    font-size: 0.9rem;
    padding: 0.65rem;
  }

  .save {
    font-size: 0.9rem;
    padding: 0.65rem 1.5rem;
  }
}

</style>