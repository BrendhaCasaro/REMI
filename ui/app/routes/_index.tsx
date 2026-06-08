import { redirect } from "react-router";

export async function clientLoader() {
  throw redirect("/medias");
}

export default function Index() {
  return null;
}
