/*
How to:

1. Get vanillatweaks trader datapack
2. Move datapack/data/wandering_trades/functions/add_trade.mcfunction here
3. Run me with nodejs
4. Get heads.json back!
*/

import fs from "fs";

let file = fs.readFileSync('./add_trade.mcfunction', 'utf8');
let lines = file.split('\n');
lines = lines.filter(line => line != '' && line.charAt() != '#'); // remove empty lines or comments
lines = lines.filter(line => line.includes("{id:\"minecraft:player_head\",Count:8")); // remove hermit heads

let heads = new Map();

let i = 1;

lines.forEach(line => {
    let uuidRegex = /(?<=SkullOwner:{Id:\[I;)(-?\d*,?)*/g;
    let uuid = line.match(uuidRegex)[0];
    let ints = uuid.split(',');
    let uuidString = formatAsUUID(ints);

    let textureRegex = /(?<={textures:\[{Value:").*(?=")/g;
    let texture = line.match(textureRegex)[0];


    let blockRegex = /(?<=,buyB:{id:").*(?=",Count:1b\})/g;
    let block = line.match(blockRegex)[0];

    console.log(i + "Added " + block);
    console.log(texture);
    console.log(uuidString);

    i++;

    let data = {
        texture: texture,
        uuid: uuidString
    };

    if(heads.has(block)) {
        heads.get(block).push(data);
    } else {
        heads.set(block, [data]);
    }

});

console.log(`Total: ` + heads.size);

i = 1;
/*
for(let e of heads.entries()) {
    console.log(i);
    console.log(e);
    i++;
}*/

fs.writeFile("heads.json", JSON.stringify(Object.fromEntries(heads), null, 4), err => {
    if(err == null) console.log("Exported json");
    else console.error("Could not export json: " + err);
})

function formatAsUUID(ints) {
    let hex = ints.map(intHex).join("").toLowerCase();
    return `${hex.substring(0, 8)}-${hex.substring(8, 12)}-${hex.substring(12,16)}-${hex.substring(16, 20)}-${hex.substring(20,32)}`;
}

function intHex(int) {
  if (int < 0) int = int >>> 0;
  return int.toString(16).padStart(8,"0").toUpperCase();
}
