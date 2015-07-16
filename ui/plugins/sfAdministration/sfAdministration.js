(function (cloudStack) {
  cloudStack.plugins.sfAdministration = function(plugin) {
    plugin.ui.addSection({
      id: 'sfAdministration',
      title: 'SolidFire Administration',
      preFilter: function(args) {
        return isAdmin();
      },
      listView: {
        id: 'sfAdministration',
        fields: {
          name: { label: 'label.name' },
          mvip: { label: 'MVIP' },
          username: { label: 'Username' },
          zonename: { label: 'label.zone' }
        },
        dataProvider: function(args) {
          plugin.ui.apiCall('listSolidFireClusters', {
            success: function(json) {
              var sfclustersfiltered = [];
              var sfclusters = json.listsolidfireclustersresponse.sfcluster;
              var search = args.filterBy.search.value == null ? "" : args.filterBy.search.value.toLowerCase();

              if (search == "") {
                sfclustersfiltered = sfclusters;
              }
              else {
                for (i = 0; i < sfclusters.length; i++) {
                  sfcluster = sfclusters[i];

                  if (sfcluster.name.toLowerCase().indexOf(search) > -1 ) {
                    sfclustersfiltered.push(sfcluster);
                  }
                }
              }

              args.response.success({ data: sfclustersfiltered });
            },
            error: function(errorMessage) {
              args.response.error(errorMessage);
            }
          });
        },
        actions: {
          add: {
            label: 'Add Reference to Cluster',
            preFilter: function(args) {
              return true;
            },
            messages: {
              confirm: function(args) {
                return 'Please fill in the following data to add a new reference to a cluster.';
              },
              notification: function(args) {
                return 'Add Reference to Cluster';
              }
            },
            createForm: {
              title: 'Add Reference to Cluster',
              desc: 'Please fill in the following data to add a new reference to a cluster.',
              fields: {
                mvip: {
                  label: 'MVIP',
                  validation: {
                    required: true
                  }
                },
                username: {
                  label: 'label.username',
                  docID: 'helpUserUsername',
                  validation: {
                    required: true
                  }
                },
                password: {
                  label: 'label.password',
                  docID: 'helpUserPassword',
                  isPassword: true,
                  validation: {
                    required: true
                  }
                },
                totalCapacity: {
                  label: 'Total Capacity',
                  validation: {
                    required: true,
                    number: true
                  }
                },
                totalMinIops: {
                  label: 'Total Min IOPS',
                  validation: {
                    required: true,
                    number: true
                  }
                },
                totalMaxIops: {
                  label: 'Total Max IOPS',
                  validation: {
                    required: true,
                    number: true
                  }
                },
                totalBurstIops: {
                  label: 'Total Burst IOPS',
                  validation: {
                    required: true,
                    number: true
                  }
                },
                availabilityZone: {
                  label: 'label.availability.zone',
                  docID: 'helpVolumeAvailabilityZone',
                  validation: {
                    required: true
                  },
                  select: function(args) {
                    $.ajax({
                      url: createURL("listZones&available=true"),
                      dataType: "json",
                      async: true,
                      success: function(json) {
                        var zoneObjs = json.listzonesresponse.zone;

                        args.response.success({
                          descriptionField: 'name',
                          data: zoneObjs
                        });
                      }
                    });
                  }
                }
              }
            },
            action: function(args) {
              var data = {
                mvip: args.data.mvip,
                username: args.data.username,
                password: args.data.password,
                totalcapacity: args.data.totalcapacity,
                totalminiops: args.data.totalminiops,
                totalmaxiops: args.data.totalmaxiops,
                totalburstiops: args.data.totalburstiops,
                availabilityzone: args.data.availabilityzone
              };

              $.ajax({
                url: createURL('createReferenceToSolidFireCluster'),
                data: data,
                success: function(json) {
                  var sfclusterObj = json.createreferencetosolidfireclusterresponse.apicreatereferencetosolidfirecluster;

                  args.response.success({
                    data: sfclusterObj
                  });
                },
                error: function(json) {
                  args.response.error(parseXMLHttpResponse(json));
                }
              });
            }
          }
        },
        detailView: {
          name: 'label.details',
          actions: {
            edit: {
              label: 'label.edit',
              messages: {
                notification: function(args) {
                  return 'Edit Cluster';
                }
              },
              action: function (args) {
                var params = [];

                params.push("&name=" + args.context.sfAdministration[0].name);
                params.push("&totalcapacity=" + args.data.totalcapacity);
                params.push("&totalminiops=" + args.data.totalminiops);
                params.push("&totalmaxiops=" + args.data.totalmaxiops);
                params.push("&totalburstiops=" + args.data.totalburstiops);

                $.ajax({
                  url: createURL('updateReferenceToSolidFireCluster' + params.join("")),
                  success: function(json) {
                    var sfclusterObj = json.updatereferencetosolidfireclusterresponse.apiupdatereferencetosolidfirecluster;

                    args.response.success({
                      data: sfclusterObj
                    });
                  },
                  error: function(json) {
                    args.response.error(parseXMLHttpResponse(json));
                  }
                });
              }
            },
            remove: {
              label: 'Delete Reference to Cluster',
              messages: {
                confirm: function(args) {
                  return 'Are you sure you would like to delete this reference to a SolidFire cluster?';
                },
                notification: function(args) {
                  return 'Delete Reference to Cluster';
                }
              },
              action: function(args) {
                $.ajax({
                  url: createURL('deleteReferenceToSolidFireCluster&name=' + args.context.sfAdministration[0].name),
                  success: function(json) {
                    args.response.success();
                  },
                  error: function(json) {
                    args.response.error(parseXMLHttpResponse(json));
                  }
                });
              }
            }
          },
          tabs: {
            details: {
              title: 'label.details',
              preFilter: function(args) {
                return [];
              },
              fields: [
                {
                  name: {
                    label: 'label.name'
                  }
                },
                {
                  uuid: {
                    label: 'label.id'
                  },
                  mvip: {
                    label: 'MVIP'
                  },
                  username: {
                    label: 'Username'
                  },
                  totalcapacity: {
                    label: 'Total Capacity',
                    isEditable: true
                  },
                  totalminiops: {
                    label: 'Total Min IOPS',
                    isEditable: true
                  },
                  totalmaxiops: {
                    label: 'Total Max IOPS',
                    isEditable: true
                  },
                  totalburstiops: {
                    label: 'Total Burst IOPS',
                    isEditable: true
                  },
                  zonename: {
                    label: 'label.zone'
                  }
                }
              ],
              dataProvider: function(args) {
                $.ajax({
                  url: createURL("listSolidFireClusters&name=" + args.context.sfAdministration[0].name),
                  dataType: "json",
                  async: true,
                  success: function(json) {
                    var jsonObj = json.listsolidfireclustersresponse.sfcluster[0];

                    args.response.success({
                      data: jsonObj
                    });
                  }
                });
              }
            }
          }
        }
      }
    });
  };
}(cloudStack));