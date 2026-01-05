# Script thats reflects the curren quarkus api to the angular frontend

cd $(dirname "$0")

url="http://localhost:8080/q/openapi"
output="openapi/service.yml"

wget -O "${output}" "${url}"
node_modules/.bin/ng-openapi-gen --input openapi/service.yml --output src/app/api  --ignoreUnusedModels=false
#
#
## Replace Root ur
file='src/app/api/api-configuration.ts'
regex="rootUrl: string = '';"
sed -i -E "s/${regex}/rootUrl: string = environment.baseUrl;/" "${file}"

## Add Import as first line
echo "import {environment} from '../../environments/environment';$(cat "${file}")" > "${file}"
