/*
 * Copyright (c) 2013, 2015 IBM Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.neutron.transcriber;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.neutron.spi.INeutronFloatingIPCRUD;
import org.opendaylight.neutron.spi.INeutronNetworkCRUD;
import org.opendaylight.neutron.spi.INeutronPortCRUD;
import org.opendaylight.neutron.spi.INeutronSubnetCRUD;
import org.opendaylight.neutron.spi.NeutronCRUDInterfaces;
import org.opendaylight.neutron.spi.NeutronFloatingIP;
import org.opendaylight.neutron.spi.NeutronSubnet;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.l3.rev141002.floatingips.attributes.Floatingips;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.l3.rev141002.floatingips.attributes.floatingips.Floatingip;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.l3.rev141002.floatingips.attributes.floatingips.FloatingipBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.rev150325.Neutron;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NeutronFloatingIPInterface extends AbstractNeutronInterface<Floatingip, NeutronFloatingIP> implements INeutronFloatingIPCRUD {
    private static final Logger LOGGER = LoggerFactory.getLogger(NeutronFloatingIPInterface.class);

    NeutronFloatingIPInterface(ProviderContext providerContext) {
        super(providerContext);
    }

    // IfNBFloatingIPCRUD interface methods

    @Override
    public boolean floatingIPExists(String uuid) {
        return exists(uuid);
    }

    @Override
    public NeutronFloatingIP getFloatingIP(String uuid) {
        return get(uuid);
    }

    @Override
    public List<NeutronFloatingIP> getAll() {
        Set<NeutronFloatingIP> allIPs = new HashSet<NeutronFloatingIP>();
        Floatingips fips = readMd(createInstanceIdentifier());
        if (fips != null) {
            for (Floatingip fip: fips.getFloatingip()) {
                allIPs.add(fromMd(fip));
            }
        }
        LOGGER.debug("Exiting getAllFloatingIPs, Found {} FloatingIPs", allIPs.size());
        List<NeutronFloatingIP> ans = new ArrayList<NeutronFloatingIP>();
        ans.addAll(allIPs);
        return ans;
    }

    @Override
    public List<NeutronFloatingIP> getAllFloatingIPs() {
        return getAll();
    }

    @Override
    public boolean addFloatingIP(NeutronFloatingIP input) {
        return add(input);
    }

    @Override
    public boolean removeFloatingIP(String uuid) {
        return remove(uuid);
    }

    @Override
    public boolean updateFloatingIP(String uuid, NeutronFloatingIP delta) {
        NeutronFloatingIP target = getFloatingIP(uuid);
        if (target == null) {
            return false;
        }
        delta.setPortUUID(target.getPortUUID());
        delta.setFixedIPAddress(target.getFixedIPAddress());
        return updateMd(delta);
    }

    @Override
    protected Floatingip toMd(String uuid) {
        FloatingipBuilder floatingipBuilder = new FloatingipBuilder();
        floatingipBuilder.setUuid(toUuid(uuid));
        return floatingipBuilder.build();
    }

    @Override
    protected Floatingip toMd(NeutronFloatingIP floatingIp) {
        FloatingipBuilder floatingipBuilder = new FloatingipBuilder();
        if (floatingIp.getFixedIPAddress() != null) {
            floatingipBuilder.setFixedIpAddress(new IpAddress(floatingIp.getFixedIPAddress().toCharArray()));
        }
        if(floatingIp.getFloatingIPAddress() != null) {
            floatingipBuilder.setFloatingIpAddress(new IpAddress(floatingIp.getFloatingIPAddress().toCharArray()));
        }
        if (floatingIp.getFloatingNetworkUUID() != null) {
            floatingipBuilder.setFloatingNetworkId(toUuid(floatingIp.getFloatingNetworkUUID()));
        }
        if (floatingIp.getPortUUID() != null) {
            floatingipBuilder.setPortId(toUuid(floatingIp.getPortUUID()));
        }
        if (floatingIp.getRouterUUID() != null) {
            floatingipBuilder.setRouterId(toUuid(floatingIp.getRouterUUID()));
        }
        if (floatingIp.getStatus() != null) {
            floatingipBuilder.setStatus(floatingIp.getStatus());
        }
        if (floatingIp.getTenantUUID() != null) {
            floatingipBuilder.setTenantId(toUuid(floatingIp.getTenantUUID()));
        }
        if (floatingIp.getID() != null) {
            floatingipBuilder.setUuid(toUuid(floatingIp.getID()));
        }
        else {
            LOGGER.warn("Attempting to write neutron floating IP without UUID");
        }
        return floatingipBuilder.build();
    }

    protected NeutronFloatingIP fromMd(Floatingip fip) {
        NeutronFloatingIP result = new NeutronFloatingIP();
        result.setID(String.valueOf(fip.getUuid().getValue()));
        if (fip.getFloatingNetworkId() != null) {
            result.setFloatingNetworkUUID(String.valueOf(fip.getFloatingNetworkId().getValue()));
        }
        if (fip.getPortId() != null) {
            result.setPortUUID(String.valueOf(fip.getPortId().getValue()));
        }
        if (fip.getFixedIpAddress() != null ) {
            result.setFixedIPAddress(String.valueOf(fip.getFixedIpAddress().getValue()));
        }
        if (fip.getFloatingIpAddress() != null) {
            result.setFloatingIPAddress(String.valueOf(fip.getFloatingIpAddress().getValue()));
        }
        if (fip.getTenantId() != null) {
            result.setTenantUUID(String.valueOf(fip.getTenantId().getValue()));
        }
        if (fip.getRouterId() != null) {
            result.setRouterUUID(String.valueOf(fip.getRouterId().getValue()));
        }
        result.setStatus(fip.getStatus());
        return result;
    }

    @Override
    protected InstanceIdentifier<Floatingip> createInstanceIdentifier(
            Floatingip item) {
        return InstanceIdentifier.create(Neutron.class)
                .child(Floatingips.class)
                .child(Floatingip.class,item.getKey());
    }

    protected InstanceIdentifier<Floatingips> createInstanceIdentifier() {
        return InstanceIdentifier.create(Neutron.class)
                .child(Floatingips.class);
    }

    public static void registerNewInterface(BundleContext context,
                                            ProviderContext providerContext,
                                            List<ServiceRegistration<?>> registrations) {
        NeutronFloatingIPInterface neutronFloatingIPInterface = new NeutronFloatingIPInterface(providerContext);
        ServiceRegistration<INeutronFloatingIPCRUD> neutronFloatingIPInterfaceRegistration = context.registerService(INeutronFloatingIPCRUD.class, neutronFloatingIPInterface, null);
        if (neutronFloatingIPInterfaceRegistration != null) {
            registrations.add(neutronFloatingIPInterfaceRegistration);
        }
    }
}
