import i18n from "i18next";
import { initReactI18next } from "react-i18next";
import LanguageDetector from "i18next-browser-languagedetector";

import commonPtBr from "./locales/pt-BR/common.json";
import loginPtBr from "./locales/pt-BR/login.json";
import mediasPtBr from "./locales/pt-BR/medias.json";
import usersPtBr from "./locales/pt-BR/users.json";
import nodesPtBr from "./locales/pt-BR/nodes.json";

import commonEn from "./locales/en/common.json";
import loginEn from "./locales/en/login.json";
import mediasEn from "./locales/en/medias.json";
import usersEn from "./locales/en/users.json";
import nodesEn from "./locales/en/nodes.json";

i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    fallbackLng: "pt-BR",
    interpolation: { escapeValue: false },
    detection: {
      order: ["localStorage", "navigator"],
      caches: ["localStorage"],
    },
    resources: {
      "pt-BR": {
        common: commonPtBr,
        login: loginPtBr,
        medias: mediasPtBr,
        users: usersPtBr,
        nodes: nodesPtBr,
      },
      en: {
        common: commonEn,
        login: loginEn,
        medias: mediasEn,
        users: usersEn,
        nodes: nodesEn,
      },
    },
  });

export default i18n;
